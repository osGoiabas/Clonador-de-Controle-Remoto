package serial;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import supervisor.Supervisor;

@SuppressWarnings("serial")
public class Protocolo extends JFrame{	
	
	public static boolean chegou = false;
	public static boolean mandouTamanho = false;
	
	public int intVsBytes[];
	public byte IRsignal[];
	public int cantoErro[];
	public int erros[];
	
	public int currentError = 0;
	public int numDeErros = 0;
	public int tamanho;
	
	private int Enviador;
	private String leituraComando;
	
	public void imprimeNoLabel(String txt){
		Supervisor.lblLabela.setText(txt);
	}
	
	public String getLeituraComando() {
		return leituraComando;
		
	}
	public void setLeituraComando(String leituraComando) { 
		this.leituraComando = leituraComando; //Colocamos a string do argumento na variavel
		this.interpretaComando(); //chamamos o metodo que interpreta a string
	}
	
	private void interpretaComando(){
		if (chegou){
			numDeErros = 0;
			String aux[] = leituraComando.split(",");		//Separa a string em substrings usando ","
			intVsBytes = new int[aux.length];
			IRsignal = new byte[aux.length];
			for (int i = 0; i < aux.length; i++){
				intVsBytes[i] = Integer.parseInt(aux[i]);
				IRsignal[i] = (byte) Integer.parseInt(aux[i]);
				if ((IRsignal[i] & 255) != intVsBytes[i] ){
					numDeErros++;
				}
			}
			
			if (numDeErros < 3){							//TALVEZ EU DEVA TIRAR ISSO
				JOptionPane.showMessageDialog(null, "Sinal muito pequeno. Clique em Capturar Sinal e tente novamente.", "Erro", JOptionPane.WARNING_MESSAGE);
				this.leituraComando = " ";
				chegou = false;
				return;
			}
			
			cantoErro = new int[numDeErros];
			erros = new int[numDeErros];
			currentError = 0;
			for (int i = 0; i < aux.length; i++){
				if ((IRsignal[i] & 255) != intVsBytes[i] ){
					cantoErro[currentError] = i;
					currentError++;
				}
			}
			currentError = 0;
			for (int i = 0; i < aux.length; i++){
				if ((IRsignal[i] & 255) != intVsBytes[i] ){
					erros[currentError] = intVsBytes[i];
					currentError++;
				}
			}
			tamanho = IRsignal.length/2;
			chegou = false;
			this.leituraComando = "<html>Para usar o botão clonado, clique em Enviar Sinal. <br> Para clonar outro botão, clique em Capturar Sinal</html>";
		}
	}
	public int getEnviador(){
		return Enviador;
	}
	public void setEnviador(int valor){
		if (valor == 21){
			this.Enviador = tamanho;
			mandouTamanho = true;
		} else if (valor == 22 && mandouTamanho){
			Supervisor.serial.sendArray(IRsignal);
			mandouTamanho = false;
		} else if (valor == 23 && !mandouTamanho){
			Supervisor.serial.sendErrorPlace(cantoErro);
			Supervisor.serial.sendErrors(erros);
		} else {
			this.Enviador = valor;
		}
	}
	
}