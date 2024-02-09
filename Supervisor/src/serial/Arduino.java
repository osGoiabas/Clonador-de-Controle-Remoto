package serial;

import supervisor.Supervisor;
import javax.swing.JButton;

public class Arduino {
	
	private Protocolo protocolo = new Protocolo();
	  /**
	   * Envia o comando para a porta serial
	   * @param button - Botão que é clicado na interface Java
	   */
	public synchronized void comunicacaoArduino(JButton button) {        
		if("Capturar Sinal".equals(button.getActionCommand())){
	    	Supervisor.serial.sendData(1);
			Supervisor.serial.sendData(2);
			Protocolo.chegou = true;
			protocolo.imprimeNoLabel("Aperte o botao");
		} else if("Enviar Sinal".equals(button.getActionCommand())){
			Supervisor.serial.sendData(3);
			Supervisor.serial.sendData(4);
			Supervisor.serial.sendData(21);
			Supervisor.serial.sendData(22);
			Supervisor.serial.sendData(23);
		 } else{
			Supervisor.serial.sendData(0);
			Supervisor.serial.close();
		 } 
}	  
}
