/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package qpar.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author thomasm
 * 
 */
public class ConfigurationFactory {

	public static final String CONFIGNAME = "qpar.conf";

	private static String[] searchPaths = { ".", System.getProperty("user.home") };

	public static Configuration getConfiguration() throws IOException {
		for (String searchPath : searchPaths) {
			String confPath = combinePaths(searchPath, CONFIGNAME);
			File confFile = new File(confPath);
			if (confFile.exists()) {
				return readConfiguration(confFile);
			}
		}
		InputStream is = ConfigurationFactory.class.getClassLoader().getResourceAsStream("DefaultConfiguration.properties");
		Properties prop = new Properties();
		prop.load(is);
		return new Configuration(prop);
	}

	public static Configuration getConfiguration(final String confPath) throws FileNotFoundException, IOException {
		return readConfiguration(new File(confPath));
	}

	private static String combinePaths(final String path1, final String path2) {
		File file1 = new File(path1);
		File file2 = new File(file1, path2);
		return file2.getPath();
	}

	private static Configuration readConfiguration(final File configFile) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(configFile));
		Configuration config = new Configuration(prop);

		return config;
	}

}
