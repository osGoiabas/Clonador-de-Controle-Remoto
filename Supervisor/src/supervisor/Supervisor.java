package supervisor;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import serial.Arduino;
import serial.SerialRxTx;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingConstants;

public class Supervisor extends JFrame {

	private static final long serialVersionUID = 1L; // nao acho que precisa disso, mas por precaucao
	public static SerialRxTx serial = new SerialRxTx();
	private JPanel contentPane;
	Arduino conn = new Arduino();
	JFrame jFrame = new JFrame();
	 public static JLabel lblLabela = new JLabel("");

	


	/** Launch the application.*/
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Supervisor frame = new Supervisor();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		if (serial.iniciaSerial()){
			while(true){}
		}
		
	}

	/** Create the frame.*/
	public Supervisor() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JButton btnModo1 = new JButton("Capturar Sinal");
		btnModo1.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnModo1.setBounds(150, 89, 137, 30);
		btnModo1.setForeground(Color.BLACK);
		btnModo1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				conn.comunicacaoArduino(btnModo1);
			}
		});
		contentPane.setLayout(null);
		contentPane.add(btnModo1);
		
		JButton btnExit = new JButton("Sair");
		btnExit.setBounds(179, 171, 83, 23);
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				conn.comunicacaoArduino(btnExit);
				System.exit(0);
			}
		});
		
		JButton btnArray = new JButton("Enviar Sinal");
		btnArray.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnArray.setBounds(150, 130, 137, 30);
		btnArray.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				conn.comunicacaoArduino(btnArray);
			}
		});
		contentPane.add(btnArray);
		contentPane.add(btnExit);
		lblLabela.setHorizontalAlignment(SwingConstants.CENTER);
		
		
		lblLabela.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblLabela.setBounds(10, 11, 414, 74);
		contentPane.add(lblLabela);

	}
}
