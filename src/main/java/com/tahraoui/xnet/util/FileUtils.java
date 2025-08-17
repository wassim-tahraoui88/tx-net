package com.tahraoui.xnet.util;

import com.tahraoui.xnet.model.FileType;

public class FileUtils {

	private static final String
			EXTENSION_JPEG = "jpeg", EXTENSION_JPG = "jpg", EXTENSION_PNG = "png", EXTENSION_GIF = "gif", EXTENSION_BMP = "bmp",
			EXTENSION_MP3 = "mp3", EXTENSION_WAV = "wav", EXTENSION_OGG = "ogg",
			EXTENSION_MP4 = "mp4", EXTENSION_AVI = "avi", EXTENSION_MKV = "mkv",
			EXTENSION_PDF = "pdf", EXTENSION_DOCX = "docx", EXTENSION_TXT = "txt",
			EXTENSION_ZIP = "zip", EXTENSION_RAR = "rar", EXTENSION_TAR = "tar";

	public static FileType getFileType(String filename) {
		var extension = getFileExtension(filename);
		return switch (extension) {
			case EXTENSION_JPEG, EXTENSION_JPG, EXTENSION_PNG, EXTENSION_GIF, EXTENSION_BMP -> FileType.IMAGE;
			case EXTENSION_MP3, EXTENSION_WAV, EXTENSION_OGG -> FileType.AUDIO;
			case EXTENSION_MP4, EXTENSION_AVI, EXTENSION_MKV -> FileType.VIDEO;
			case EXTENSION_PDF, EXTENSION_DOCX, EXTENSION_TXT -> FileType.DOC;
			case EXTENSION_ZIP, EXTENSION_RAR, EXTENSION_TAR -> FileType.ARCHIVE;
			default -> FileType.OTHER;
		};
	}

	public static String getFileExtension(String filename) {
		int lastIndex = filename.lastIndexOf('.');
		if (lastIndex == -1 || lastIndex == filename.length() - 1) return "";
		return filename.substring(lastIndex + 1).toLowerCase();
	}
}
