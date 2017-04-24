package wftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import wftp.views.ConnView;
import wftp.views.LocalView;

public class Context {
	
	private static Context instance = new Context();
	private File cfgFile;
	private Document doc;
	private ConnView connView;
	private LocalView localView;
	
	
	public ConnView getConnView() {
		return connView;
	}
	
	public void setConnView(ConnView connView) {
		this.connView = connView;
	}

	public LocalView getLocalView() {
		return localView;
	}

	public void setLocalView(LocalView localView) {
		this.localView = localView;
	}
	
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public File getCfgFile() {
		return cfgFile;
	}

	public void setCfgFile(File cfgFile) {
		this.cfgFile = cfgFile;
	}

	private Context() {
		init();
	}

	private void init() {
		readConfig();
	}

	private void initCfgFile() {
		doc = DocumentFactory.getInstance().createDocument();
		Element root = DocumentFactory.getInstance().createElement("wftp");
		doc.setRootElement(root);
		storeConfig();
	}

	private void readConfig() {
		try {
			File stateLocation = Activator.getDefault().getStateLocation().makeAbsolute().toFile();
			cfgFile = new File(stateLocation,"wftp.cfg.xml");
			if (!cfgFile.exists()) {
				cfgFile.createNewFile();
				initCfgFile();
			}
			SAXReader reader = new SAXReader();
			FileInputStream in = new FileInputStream(cfgFile);
			InputStreamReader isr = new InputStreamReader(in, "UTF-8");
			doc = reader.read(isr);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	public void storeConfig() {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		XMLWriter cfgWriter;
		try {
			cfgWriter = new XMLWriter(new FileOutputStream(cfgFile), format);
			cfgWriter.write(doc);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Context getInstance() {
		return instance;
	}

}
