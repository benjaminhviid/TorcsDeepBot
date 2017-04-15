package itu.bj.torcs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

public class DataCollector {

	private static final DataCollector _instance = new DataCollector();

	private DataCollector(){
		if (getLatestID == -1)
			screenCount = 1;
		else 
			screenCount = getLatestID;
	 }

	public static DataCollector instance(){
		return _instance;
	}

	private static String path = "data";
	private static String newFolder = "/training_data";
	private static String newDir = "";
	private static FileWriter fw;
	private static Double _angle = 0d;
	private static Double _dist_RL = 0d;
	private static Double _dist_RR = 0d;
	private static Double _speed = 0d;
	private static double _height = 0d;
	private static double _width = 0d;
	private static Double _id = 0d;
	private static boolean recording = false;

	private static int width = 800;
	private static int offsetY = 52;
	private static int height = 600;
	private static boolean pause = false;
	private static boolean useMainDir = false;

	public static void main(String[] args) {

	}


	public static void Pause(){
		pause = true;
		recording = false;
	}

	public static void Resume(){
		pause = false;
		recording = false;
	}

	public void UpdateTrainingValues(Double angle, Double dist_RL, Double dist_RR, Double speed){
		_angle = angle;
		_dist_RL = dist_RL;
		_dist_RR = dist_RR;
		_speed = speed;
	}

	public static void StartDataCollection(Integer ms){
		System.out.println("Beginning data collection");
		recording = true;
		newDir = path + newFolder;

		if (useMainDir){
			boolean filenameExist = new File(newDir.toString()).exists();

			if (filenameExist){
				try {
					fw = new FileWriter(newDir + "/trainingdata.csv"), true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ScheduleDataCollection(ms);
			}


		}

		else{
			Integer counter = 1;
			boolean filenameExist = new File(newDir + counter.toString()).exists();
			while (filenameExist){
				counter++;
				filenameExist = new File(newDir + counter.toString()).exists();
			}
			newDir += counter.toString();
			boolean success = (new File(newDir)).mkdirs();
			if (success) {
				try {
					fw = new FileWriter(new File(newDir + "/trainingdata.csv"), true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ScheduleDataCollection(ms);
			}
			else {
				System.out.println("Training data folder could not be created");
			}
		}
		
	}

	private static void ScheduleDataCollection(Integer ms){

		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run() {
				if (pause){
					return;
				}
				GetData();
			}
		}, 0, ms);
	}

	static Long counter = 0L;

	static void GetData(){
		try {
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(0,offsetY, width, height));
			BufferedImage resized = resize(image, 280, 210);

			String screenshotFolder = "/screenshots";
			String screenshotDir = newDir + screenshotFolder;

			if (!(new File(screenshotDir).exists())){
				new File(screenshotDir).mkdirs();
			}
			ImageIO.write(resized, "jpg", new File(screenshotDir + "/screenshot"+screenCount+".jpg"));
			screenCount++;
			_id = screenCount.doubleValue();
			_width = image.getWidth();
			_height = image.getHeight();
		} catch (HeadlessException | AWTException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void WriteSampleToCSV (byte[] pixelData){

		try {
			System.out.println("Writing sample " + _id);
			fw = new FileWriter(newDir + "/trainingdata.csv", true);
			StringBuilder sb = new StringBuilder();
			sb.append(_angle.toString());
			sb.append(';');
			sb.append(_speed.toString());
			sb.append(';');
			sb.append(_dist_RL.toString());
			sb.append(';');
			sb.append(_dist_RR.toString());
			sb.append(';');
			sb.append(_height);
			sb.append(';');
			sb.append(_width);
			sb.append(';');
			sb.append(_id.toString());
			sb.append('\n');
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getLatestID(String fileDir) {

 		File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    if (files == null || files.length == 0) {
	        return -1;
	    }

	    File lastModifiedFile = files[0];
	    for (int i = 1; i < files.length; i++) {
	       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	       }
	    }

	    String name = FilenameUtils.removeExtension(lastModifiedFile.getName());
	    int id = Integer.parseInt(name.replace("screenshot", ""));

	    return id;
}


	public boolean isRecording(){
		return recording;
	}
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

}