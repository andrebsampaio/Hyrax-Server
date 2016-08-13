package edu.thesis.web.service.rest;

import java.io.File;
import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

public class MyListener implements ServletContextListener {

	private static String mServiceName = "hyrax";
	private static final String SERVICE_TYPE = "_http._tcp.local.";
	JmDNS jmdns;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			if (jmdns != null) {
				jmdns.unregisterAllServices();
				jmdns.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		new Thread() {
			public void run() {
				
				try {
					jmdns = JmDNS.create();
					ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, mServiceName, 8080, "B");
					jmdns.registerService(info);
					System.out.println("INITIATED" + jmdns.getHostName());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();

	}
}