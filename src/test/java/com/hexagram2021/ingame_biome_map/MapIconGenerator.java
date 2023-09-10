package com.hexagram2021.ingame_biome_map;

import com.google.gson.JsonObject;
import com.hexagram2021.ingame_biome_map.utils.ConfigHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class MapIconGenerator {
	private static final Base64.Encoder base64Encoder = Base64.getEncoder();
	
	public static void main(String[] args) {
		String outer = "src/test/java/com/hexagram2021/ingame_biome_map";
		String directoryPath = outer + "/map_icons";
		
		File directory = new File(directoryPath);
		File[] files = directory.listFiles();
		
		if(files != null) {
			for (File file : files) {
				if(file.isFile()) {
					String imageFileName = file.getAbsolutePath();
					ByteArrayOutputStream baos = null;
					try {
						String suffix = imageFileName.substring(imageFileName.lastIndexOf('.') + 1);
						File imageFile = new File(imageFileName);
						BufferedImage bufferedImage = ImageIO.read(imageFile);
						baos = new ByteArrayOutputStream();
						ImageIO.write(bufferedImage, suffix, baos);
						byte[] bytes = baos.toByteArray();
						String filename = file.getName();
						String id = filename.substring(0, filename.lastIndexOf('.'));
						System.out.println(id);
						String base64 = base64Encoder.encodeToString(bytes);
						File generated = new File(outer + "/generated/" + id + ".json");
						if(!generated.exists() && !generated.createNewFile()) {
							continue;
						}
						JsonObject json = new JsonObject();
						json.addProperty("id", "minecraft:" + id);
						json.addProperty("base64", base64);
						try(FileOutputStream fos = new FileOutputStream(generated)) {
							try(OutputStreamWriter writer = new OutputStreamWriter(fos)) {
								ConfigHelper.writeJsonToFile(writer, null, json, 0);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (baos != null) {
								baos.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
