package com.jhonnymontoya.speech;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

public class Main extends JFrame implements WindowListener {

	private static final long serialVersionUID = 1L;

	private LiveSpeechRecognizer reconocedor;
	private ExecutorService ejecutor = Executors.newFixedThreadPool(2);

	private boolean hiloReconocedor = false;
	private boolean hiloRecurso;

	private JLabel estado;
	private JTextArea resultado;
	
	private JScrollPane scroll;

	public Main() {
		super("Reconocedor de voz");
		this.setLayout(null);
		this.setBounds(200, 150, 1000, 400);
		
		this.estado = new JLabel("Cargando....");
		this.estado.setForeground(new Color(255, 0, 0));
		this.estado.setBounds(10, 10, this.getWidth() - 40, 20);
		this.add(this.estado);
		
		this.resultado = new JTextArea();
		this.scroll = new JScrollPane(this.resultado);
		this.scroll.setBounds(10, 35, this.getWidth() - 40, this.getHeight() - 85);
		this.add(this.scroll);
		
		
		this.setVisible(true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		
		
		Configuration configuration = new Configuration();

		/*configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");*/
		
		configuration.setAcousticModelPath("resource:/es-es/es-es");
		configuration.setDictionaryPath("resource:/es-es/es.dict");
		configuration.setLanguageModelPath("resource:/es-es/es-20k.lm");

		try {
			this.reconocedor = new LiveSpeechRecognizer(configuration);
		}
		catch (IOException ex) {
		}

		startResourcesThread();
		startSpeechRecognition();
	}

	public synchronized void startSpeechRecognition() {
		if(this.hiloReconocedor) return;
		this.ejecutor.submit(() -> {
			this.hiloReconocedor = true;

			this.reconocedor.startRecognition(true);

			//INFO DE QUE PUEDE EMPEZAR A HABLAR
			this.estado.setText("Puede empezar a hablar");
			this.estado.setForeground(new Color(0, 0, 255));

			try {
				while(this.hiloReconocedor) {
					SpeechResult speechResult = reconocedor.getResult();
					if(speechResult == null) {
						//NO ENTENDÏ LO QUE DIJÓ
					}
					else {
						String resultado = speechResult.getHypothesis();
						mostrar(resultado, speechResult.getWords());
					}
				}
			}
			catch(Exception ex) {
				this.hiloReconocedor = false;
			}
		});
	}

	public void startResourcesThread() {
		if(this.hiloRecurso) return;
		this.ejecutor.submit(() -> {
			try {
				this.hiloRecurso = true;
				while(true) {
					if(!AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
						//No está disponible el microfono
					}
					Thread.sleep(350);
				}
			}
			catch(InterruptedException ex) {
				this.hiloRecurso = false;
			}
		});
	}

	public void mostrar(String texto, List<WordResult> palabras) {
		Iterator<WordResult> iPalabras = palabras.iterator();
		if(palabras.size() > 0) {
			System.out.println("====================================");
		}
		while(iPalabras.hasNext()) {
			WordResult wr = iPalabras.next();
			double confidence = wr.getConfidence();
			String pronunciacion = wr.getPronunciation().getWord().getSpelling();
			double score = wr.getScore();
			String palabra = wr.getWord().toString();
			String res = String.format("Palabra = %s, Pronunciacion = %s, Confidence = %s, Score = %s", palabra, pronunciacion, confidence, score);
			System.out.println(res);
		}
		if(palabras.size() > 0) {
			System.out.println("====================================\n\n");
		}
		//palabras.get(0).getPronunciation().toString();
		String tmp = this.resultado.getText();
		if(tmp.length() > 0) {
			tmp = tmp + "\n" + texto;
		}
		else {
			tmp = texto;
		}
		this.resultado.setText(tmp);
		JScrollBar vertical = this.scroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		this.ejecutor.shutdown();
		System.exit(NORMAL);
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
