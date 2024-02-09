package serial;

//imports
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


public class SerialRxTx implements SerialPortEventListener{
	
	SerialPort serialPort = null;
	private Protocolo protocolo = new Protocolo(); 	//objeto de gestao do protocolo
	private String appName; 	//nome da aplicacao
	
	private BufferedReader input; 	//objeto para leitura na serial
	private OutputStream output;	//objeto para escrita na serial
	
	private static final int TIME_OUT  = 1000; 	//tempo de espera por dados
	private static int DATA_RATE = 9600; 		//9600 baud, ou seja 9600 bits por seg.
	
	private String serialPortName = "COM3";
		
	
	//Metodo que inicia a porta Serial
	public boolean iniciaSerial(){
		boolean status = false;
		try{
			CommPortIdentifier portId = null;
			@SuppressWarnings("rawtypes") //tava dando um aviso, isso aqui resolveu
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers(); //Obtem portas seriais do sistemas
			
			while (portId == null && portEnum.hasMoreElements()){
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				if (currPortId.getName().equals(serialPortName) || currPortId.getName().startsWith(serialPortName)){
																					//Se achou uma porta com o nome que queremos,
					serialPort = (SerialPort) currPortId.open(appName, TIME_OUT); 	//Abra essa porta 
					portId = currPortId; 											//preencha o ID do port (saindo do while)
					protocolo.imprimeNoLabel("Conectado em: " + currPortId.getName()); //Confirmacao para o usuario
					break;
				}
			}
			
			if (portId == null || serialPort == null){
				return false;
			}
			
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			status = true;
			
			try{
				Thread.sleep(1000);
			} catch	(InterruptedException e){
				e.printStackTrace();
				status = false;
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		return status;
	}
	
	//Metodo que envia dados pela Serial
	public void sendData (int data){
		try {
			protocolo.setEnviador(data);
			output = serialPort.getOutputStream();
			output.write(protocolo.getEnviador());
		} catch (Exception e) {
			System.err.println(e.toString());
			protocolo.imprimeNoLabel("Deu erro no envio");
		}
		
	}
	
	//Metodo que manda um Array pro Arduino
	public void sendArray (byte[] data){
		try {
			for (int x=0; x<data.length-1; x++){
				output = serialPort.getOutputStream();
				output.write(data[x]);
			}
		} catch (Exception e) {
			System.err.println(e.toString());
			protocolo.imprimeNoLabel("Deu erro no envio do Array");
		}
	}
	
	//Metodo que envia o numero e a posicao dos erros 
	public void sendErrorPlace(int[] cantoEho){
		byte numero = (byte) cantoEho.length;
		try {
			output.write(8);
			output.write(numero);
			for (int i=0; i<cantoEho.length; i++){
				output.write(cantoEho[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Metodo que manda os erros
	public void sendErrors(int[] ehos){
		
		for (int i = 0; i<ehos.length; i++){
			try {
				short x = (short) ehos[i];
				byte upper = (byte) (x >> 8); 
				byte lower = (byte) (x & 0xFF); //BITWISE AND entre x e 255
				output.write(upper); 
				output.write(lower);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	//Metodo que recebe dados pela Serial
	@Override
	public void serialEvent(SerialPortEvent spe) {
		try {
			switch (spe.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE: //caso o valor do evento seja igual ao dado recebido...
				if (input == null){ //Se o objeto de input nao foi criado ainda...
					//pegue os dados recebidos, e os passe como parametro pro leitor, 
					//que por sua vez eh o parametro do leitor bufferizado
					input = new BufferedReader(new InputStreamReader(serialPort.getInputStream())); 
				}
				if (input.ready()){ //se o leitor estiver pronto...
					//chame o metodo setter usando uma linha inteira do input como argumento
					protocolo.setLeituraComando(input.readLine());
					//Imprima esses dados processados				
					protocolo.imprimeNoLabel(protocolo.getLeituraComando());
				}
				break;
			default:
				break;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		
	//Metodo que fecha a porta Serial
	public synchronized void close(){
		if (serialPort != null){
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
}