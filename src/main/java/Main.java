import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TooManyListenersException;
import java.util.TreeSet;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;

import com.buttons.simple.SimpleButton;
import com.comboBox.comboSuggestion.ComboBoxSuggestion;
import com.spinner.simple.Spinner;
import com.textField.simple.TextField;

import checkbox.CheckBoxCustom;
import drag_and_drop.DragAndDrop;
import drag_and_drop.UtilDragAndDrop;
import mthos.JMthos;
import textarea.TextAreaScroll;

@SuppressWarnings("all")

public class Main extends javax.swing.JFrame {

	private static Runnable runnable;

	private TextField extensiones;

	private JLabel lblNewLabel_3;

	private final ComboBoxSuggestion tipoFile;

	private ArrayList<String> lecturaTxt;

	private DragAndDrop panel;

	private static Spinner dias;

	private static TextAreaScroll listArchivos;

	private static ComboBoxSuggestion comboBox_1;

	private CheckBoxCustom carpeta;

	private static SimpleButton btnNewButton_1;

	private static Spinner velocidad;

	private File archivoParaConfig;

	private FileWriter fw;

	private final String ARCHIVOCONFIG = "archivos_para_borrar.txt";

	private static LocalDate d2;

	private static LocalDate d1;

	private static Duration diff;

	private static String fechaElejida;

	private static BasicFileAttributes attr;

	static Timer t;

	static MyTask mTask;

	private void addFile(String lectura, String opcion, BufferedWriter bw) throws IOException {

		if (listArchivos.getText().isEmpty()) {

			listArchivos.setText(lectura + opcion);

			bw.write(lectura + opcion);

			lecturaTxt.add(lectura + opcion);

		}

		else if (!lecturaTxt.contains(lectura + opcion)) {

			listArchivos.setText(listArchivos.getText() + "\n\n" + lectura + opcion);

			bw.write("\n" + lectura + opcion);

			lecturaTxt.add(lectura + opcion);

		}

	}

	public static void repetir() {

		t = new Timer();

		mTask = new MyTask();

		int period = velocidad.getValor() * 1000;

		if (period <= 0) {

			mTask.cancel();
			btnNewButton_1.setText("Iniciar");
		}

		else {
			btnNewButton_1.setText("Parar");

			t.scheduleAtFixedRate(mTask, 0, period);

		}

	}

	public static String extraerExtension(String nombreArchivo) {

		String extension = "";

		if (nombreArchivo.length() >= 3) {

			extension = nombreArchivo.substring(
					nombreArchivo.length() - nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).length());

		}

		return extension.toLowerCase();

	}

	public Main() throws IOException, TooManyListenersException {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/img/Recycle_Bin_Full.png")));

		getContentPane().setBackground(new Color(226, 226, 226));

		this.tipoFile = new ComboBoxSuggestion();

		setTitle("AutoBorrame");

		initComponents();

		JMthos.crearFichero("Config" + JMthos.saberSeparador() + ARCHIVOCONFIG);

		leerArchivoConfig();

		escribirListaDeArchivoDeConfiguracion();

		try {

			LinkedList<String> antiguedad = new LinkedList();

			antiguedad = (LinkedList<String>) JMthos.leerArchivo("Config" + JMthos.saberSeparador() + "antiguedad.txt");

			if (!antiguedad.isEmpty()) {

				dias.setValor(Integer.parseInt(antiguedad.get(0)));

				velocidad.setValor(Integer.parseInt(antiguedad.get(1)));

			}

		}

		catch (Exception e) {

		}

		setVisible(true);

		repetir();

	}

	private void escribirListaDeArchivoDeConfiguracion() {

		for (int i = 0; i < lecturaTxt.size(); i++) {

			if (i == 0) {

				listArchivos.setText(lecturaTxt.get(i));

			}

			else {

				listArchivos.setText(listArchivos.getText() + "\n\n" + lecturaTxt.get(i));

			}

		}

	}

	private void leerArchivoConfig() {

		try {

			archivoParaConfig = new File(JMthos.rutaActual() + "Config" + JMthos.saberSeparador() + ARCHIVOCONFIG);

			fw = new FileWriter(archivoParaConfig, true);

			BufferedReader br = new BufferedReader(new FileReader(archivoParaConfig));

			lecturaTxt = new ArrayList<>();

			String strng;

			while ((strng = JMthos.eliminarEspacios(br.readLine(), false)) != null) {

				if (!strng.isEmpty()) {

					lecturaTxt.add(strng);

				}

			}

		}

		catch (Exception e) {

		}

	}

	public static void main(String[] args) {

		try {

			new Main().setVisible(true);

		}

		catch (Exception e) {

		}

	}

	public static void iniciarBorradoAutomatico() {

		try {

			JMthos.crearFichero(JMthos.directorioActual() +

					"Config" + JMthos.saberSeparador() + "antiguedad.txt",
					dias.getValor() + "\n" + velocidad.getValor());

			borrarArchivos(dias.getValor(), velocidad.getValor());

		}

		catch (Exception e1) {

		}
	}

	private static void borrarArchivos(int dias, int velocidad) {

		try {

			String[] archivos = listArchivos.getText().split("\n");

			SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList(archivos));

			File archivoParaBorrar;

			String fechaElejida = "";

			Calendar c = Calendar.getInstance();

			BasicFileAttributes attr;

			d2 = JMthos.hoy("-", true, true);

			String filtro = "";

			File comprobacion;

			for (String archivo : sortedSet) {

				if (!archivo.isEmpty() && archivo.contains("▀")) {

					filtro = archivo.substring(archivo.indexOf("▀") + 1, archivo.length()).trim();

					archivo = archivo.substring(0, archivo.indexOf("▀")).trim();

					archivoParaBorrar = new File(archivo);

					if (archivoParaBorrar.exists() && archivoParaBorrar.isFile()) {

						borrarFicheroCaducidad(dias, archivoParaBorrar, d2);

					}

					else {

						if (!archivo.endsWith(JMthos.saberSeparador())) {

							archivo += JMthos.saberSeparador();

						}

						for (String valor : JMthos.listar(archivo, filtro, false)) {

							comprobacion = new File(valor);

							if (archivoParaBorrar.exists() && comprobacion.isFile()) {

								borrarFicheroCaducidad(dias, comprobacion, d2);

							}

						}

					}

				}

			}

		}

		catch (Exception e2) {

		}

	}

	private boolean comprobarValidez(String lectura, String opcion) {

		boolean resultado = false;

		try {

			File archivo = new File(lectura);

			if (archivo.isFile()) {

				switch (opcion) {

				case "all":

					resultado = true;

					break;

				case "images":

					if (JMthos.esImagen(lectura)) {

						resultado = true;

					}

					break;

				case "videos":

					if (JMthos.esVideo(lectura)) {

						resultado = true;

					}

					break;

				default:

					String[] extensiones = opcion.split(",");

					ArrayList<String> lista = new ArrayList();

					for (int i = 0; i < extensiones.length; i++) {

						extensiones[i] = JMthos.eliminarEspacios(extensiones[i], true);

						if (!extensiones[i].isEmpty() && JMthos.cumpleLaExpresionRegular(extensiones[i], "^[a-z]{3,4}$")
								&& lectura.endsWith("." + extensiones[i])) {

							resultado = true;

							i = extensiones.length;

						}

					}

					break;

				}

			}

		}

		catch (Exception e) {

		}

		return resultado;

	}

	private static void borrarFicheroCaducidad(int dias, File archivoParaBorrar, LocalDate d2) throws IOException {
		try {
			attr = Files.readAttributes(Paths.get(archivoParaBorrar.getAbsolutePath()), BasicFileAttributes.class);

			switch (comboBox_1.getSelectedIndex()) {

			case 0:

				fechaElejida = attr.lastModifiedTime().toString();

				break;

			case 1:

				fechaElejida = attr.creationTime().toString();

				break;

			default:

				fechaElejida = attr.lastAccessTime().toString();

				break;

			}

			fechaElejida = fechaElejida.substring(0, fechaElejida.indexOf("T"));

			d1 = LocalDate.parse(fechaElejida, DateTimeFormatter.ISO_LOCAL_DATE);

			diff = Duration.between(d1.atStartOfDay(), d2.atStartOfDay());

			if (diff.toDays() >= dias) {

				archivoParaBorrar.delete();

			}

		}

		catch (Exception e) {

		}

	}

	public void initComponents() throws IOException, TooManyListenersException {

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

		setResizable(false);

		dias = new Spinner();

		carpeta = new CheckBoxCustom("Seleccionar todos los archivos de la carpeta raiz");

		panel = new DragAndDrop("Arrastra archivos o carpetas", "Arrastre aqui los archivos");

		panel.setBorder(new LineBorder(Color.BLACK));

		panel.setFont(new Font("Tahoma", Font.PLAIN, 20));

		panel.setHorizontalAlignment(SwingConstants.CENTER);

		new UtilDragAndDrop(panel, panel.dragBorder, true, new UtilDragAndDrop.Listener() {

			@Override
			public void filesDropped(java.io.File[] archivos) {

				try {

					leerArchivoConfig();

					String lectura;

					String opcion = " ▀ ";

					boolean comprobarCarpeta = carpeta.isSelected();

					String extension = "";

					extensiones.setText(extensiones.getText().toLowerCase());

					switch (tipoFile.getSelectedIndex()) {

					case 0:

						extension = "all";

						break;

					case 1:

						extension = "images";

						break;

					case 2:

						extension = "videos";

						break;

					default:

						extension = JMthos.eliminarEspacios(extensiones.getText(), true);

						break;

					}

					opcion += extension;

					if (!comprobarCarpeta) {

						opcion = opcion.replace("all", "one");

					}

					BufferedWriter bw = new BufferedWriter(fw);

					for (File f : archivos) {

						if (comprobarCarpeta) {

							lectura = JMthos.extraerCarpeta(f.getAbsolutePath().toString());

						}

						else {

							lectura = f.getAbsolutePath().toString();

						}

						if (comprobarCarpeta || f.isDirectory()
								|| (!comprobarCarpeta && f.isFile()
										&& (comprobarValidez(lectura, extension) && (lecturaTxt.isEmpty()
												|| (!lecturaTxt.isEmpty() && !lecturaTxt.contains(lectura)))))) {

							addFile(lectura, opcion, bw);

						}

					}

					bw.close();

				}

				catch (Exception e1) {

				}

			}

		});

		listArchivos = new TextAreaScroll();

		listArchivos.setLabelText("Archivos y carpetas a borrar");

		listArchivos.setEditable(false);

		dias.setFont(new Font("Tahoma", Font.PLAIN, 18));

		dias.setValor(7);

		dias.setNegativo(false);

		JLabel lblNewLabel = new JLabel("Borrar con antig\u00FCedad de");
		lblNewLabel.setIcon(new ImageIcon(Main.class.getResource("/img/date.png")));

		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);

		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));

		JLabel lblNewLabel_1 = new JLabel("segundos");

		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);

		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 20));

		JLabel lblNewLabel_2 = new JLabel("Borrar por tipo de archivo");

		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);

		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 20));

		extensiones = new TextField();

		extensiones.setHorizontalAlignment(SwingConstants.CENTER);

		tipoFile.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				extensiones.setEditable(false);

				switch (tipoFile.getIndex()) {

				case 0:

					extensiones.setText("all");

					break;

				case 1:

					extensiones.setText("jpg,png,apng,bmp,gif,jpeg,webp,jfif,avif");

					break;

				case 2:

					extensiones.setText("mp4,avi,mpg,mkv,mov,webm");

					break;

				case 3:

					extensiones.setText("");

					extensiones.setEditable(true);

					break;

				}

			}

		});

		tipoFile.setFont(new Font("Tahoma", Font.PLAIN, 20));

		tipoFile.addItem("Todos los archivos");

		tipoFile.addItem("Imagenes");

		tipoFile.addItem("Videos");

		tipoFile.addItem("Especificar extension");

		extensiones.setFont(new Font("Tahoma", Font.PLAIN, 20));

		extensiones.setColumns(10);

		lblNewLabel_3 = new JLabel("Poner extensiones usando la coma ej: jpg,png");

		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);

		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 20));

		comboBox_1 = new ComboBoxSuggestion();

		comboBox_1.setFont(new Font("Tahoma", Font.PLAIN, 20));

		comboBox_1.addItem("Fecha de modificación");

		comboBox_1.addItem("Fecha de creación");

		comboBox_1.addItem("Último acceso");

		JLabel lblNewLabel_3_1 = new JLabel("Borrar archivos con la fecha de");

		lblNewLabel_3_1.setHorizontalAlignment(SwingConstants.CENTER);

		lblNewLabel_3_1.setFont(new Font("Tahoma", Font.PLAIN, 20));

		carpeta.setFont(new Font("Tahoma", Font.PLAIN, 20));

		SimpleButton btnNewButton = new SimpleButton("Limpiar");

		btnNewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					fw.close();

					JMthos.eliminarFichero(
							JMthos.directorioActual() + "Config" + JMthos.saberSeparador() + ARCHIVOCONFIG);

					JMthos.crearFichero("Config" + JMthos.saberSeparador() + ARCHIVOCONFIG);

					listArchivos.setText("");

					lecturaTxt.clear();

				}

				catch (Exception e1) {

				}

			}

		});

		btnNewButton.setBorderColor(Color.LIGHT_GRAY);

		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 20));

		btnNewButton_1 = new SimpleButton("Iniciar");

		btnNewButton_1.setText("Parar");

		btnNewButton_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (btnNewButton_1.getText().equals("Parar")) {

					mTask.cancel();

					t = null;

					btnNewButton_1.setText("Iniciar");

				}

				else {

					btnNewButton_1.setText("Parar");

					repetir();

				}

			}

		});

		btnNewButton_1.setFont(new Font("Tahoma", Font.PLAIN, 20));

		JLabel lblNewLabel_4 = new JLabel("Auto borrar cada");
		lblNewLabel_4.setIcon(new ImageIcon(Main.class.getResource("/img/Recycle_Bin_Full.png")));
		lblNewLabel_4.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_4.setFont(new Font("Tahoma", Font.PLAIN, 20));

		velocidad = new Spinner();

		velocidad.setValor(60);

		velocidad.setNegativo(false);

		velocidad.setFont(new Font("Tahoma", Font.PLAIN, 18));

		JLabel lblNewLabel_1_1 = new JLabel("d\u00EDas");
		lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.PLAIN, 20));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(27)
						.addGroup(layout.createParallelGroup(Alignment.TRAILING)
								.addComponent(listArchivos, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
								.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
								.addComponent(panel, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
						.addGap(18)
						.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel_3_1, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
								.addComponent(extensiones, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
								.addComponent(lblNewLabel_3, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
								.addComponent(tipoFile, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
								.addComponent(lblNewLabel_2, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
								.addComponent(comboBox_1, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
								.addGroup(layout
										.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.TRAILING)
												.addGroup(layout.createSequentialGroup()
														.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 327,
																Short.MAX_VALUE)
														.addGap(18).addComponent(dias, GroupLayout.PREFERRED_SIZE, 108,
																GroupLayout.PREFERRED_SIZE))
												.addGroup(layout.createSequentialGroup()
														.addComponent(lblNewLabel_4, GroupLayout.DEFAULT_SIZE, 341,
																Short.MAX_VALUE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(velocidad, GroupLayout.PREFERRED_SIZE, 108,
																GroupLayout.PREFERRED_SIZE)))
										.addGap(18)
										.addGroup(layout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblNewLabel_1_1, GroupLayout.PREFERRED_SIZE, 123,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 123,
														GroupLayout.PREFERRED_SIZE)))
								.addGroup(layout.createSequentialGroup()
										.addComponent(carpeta, GroupLayout.PREFERRED_SIZE, 461,
												GroupLayout.PREFERRED_SIZE)
										.addGap(18)
										.addComponent(btnNewButton_1, GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)))
						.addGap(39)));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(layout.createSequentialGroup().addGap(11)
								.addGroup(layout.createParallelGroup(Alignment.TRAILING)
										.addComponent(carpeta, GroupLayout.PREFERRED_SIZE, 66,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 63,
												GroupLayout.PREFERRED_SIZE)))
						.addGroup(Alignment.TRAILING,
								layout.createSequentialGroup().addGap(18).addComponent(panel, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(Alignment.BASELINE)
										.addComponent(dias, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblNewLabel_1_1, GroupLayout.PREFERRED_SIZE, 42,
												GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED, 10, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
										.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)))
						.addGroup(layout.createParallelGroup(Alignment.TRAILING).addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(Alignment.LEADING)
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblNewLabel_4, GroupLayout.DEFAULT_SIZE, 94,
																Short.MAX_VALUE)
														.addComponent(velocidad, GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addGap(20))
										.addGroup(layout.createSequentialGroup().addGap(27).addComponent(lblNewLabel_1)
												.addPreferredGap(ComponentPlacement.RELATED)))
								.addComponent(lblNewLabel_2).addGap(10)
								.addComponent(tipoFile, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
								.addGap(37).addComponent(lblNewLabel_3).addGap(9)
								.addComponent(extensiones, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(28)
								.addComponent(lblNewLabel_3_1, GroupLayout.PREFERRED_SIZE, 25,
										GroupLayout.PREFERRED_SIZE)
								.addGap(8)
								.addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
								.addGroup(layout.createSequentialGroup().addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(listArchivos, GroupLayout.PREFERRED_SIZE, 407,
												GroupLayout.PREFERRED_SIZE)))
						.addGap(38)));

		getContentPane().setLayout(layout);

		setSize(new Dimension(957, 657));

		setLocationRelativeTo(null);

	}

	public void actionPerformed(ActionEvent arg0) {

	}

	public void stateChanged(ChangeEvent e) {

	}

}
