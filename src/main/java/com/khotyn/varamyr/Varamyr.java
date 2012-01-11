package com.khotyn.varamyr;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * User: khotyn
 * Date: 12-1-10
 * Time: PM10:19
 */
public class Varamyr {
    public static final byte[] font = ("\n<style type='text/css'>\n" +
            "@page {\n" +
            "    margin-bottom: 5pt;\n" +
            "    margin-top: 5pt\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: normal;\n" +
            "    font-style: normal;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: bold;\n" +
            "    font-style: normal;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: normal;\n" +
            "    font-style: italic;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: bold;\n" +
            "    font-style: italic;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n</style>").getBytes();

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("Please input the epub file path you want to convert!");
            return;
        }

        try {
            new Varamyr().parse(args[0]);
        } catch (IOException e) {
            System.out.println("Malformed epub file!!");
        } catch (ParserConfigurationException e) {
            System.out.println("Malformed epub file!!");
        } catch (SAXException e) {
            System.out.println("Malformed epub file!!");
        }
    }

    public void parse(String filePath) throws IOException, ParserConfigurationException, SAXException {
        if (!filePath.endsWith(".epub")) {
            System.out.println("Please provide an epub file");
            return;
        }

        ZipFile book = new ZipFile(filePath);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath.replace(".epub", "-varamyr.epub")));
        Enumeration entries = book.entries();
        List<String> htmls = new ArrayList<String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        byte[] buf = new byte[1024];

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();

            if (zipEntry.getName().endsWith(".opf")) {
                Document document = db.parse(book.getInputStream(zipEntry));
                Element element = document.getDocumentElement();

                NodeList nodeList = element.getElementsByTagName("item");

                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element el = (Element) nodeList.item(i);

                        if (el.getAttribute("media-type").equals("application/xhtml+xml")) {
                            htmls.add(el.getAttribute("href"));
                        }
                    }
                }
            }
        }

        entries = book.entries();

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            String path = zipEntry.getName().substring(zipEntry.getName().lastIndexOf('/') + 1);

            if (htmls.contains(path)) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(book.getInputStream(zipEntry)));
                String line = null;
                out.putNextEntry(new ZipEntry(zipEntry.getName()));

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("<head>")) {
                        int position = line.indexOf("<head>") + "<head>".length();
                        out.write(line.substring(0, position).getBytes());
                        out.write(font);
                        out.write(line.substring(position).getBytes());
                    } else {
                        out.write(line.getBytes());
                    }
                }

                out.closeEntry();
                bufferedReader.close();
            } else {
                InputStream in = book.getInputStream(zipEntry);
                out.putNextEntry(new ZipEntry(zipEntry.getName()));
                int len = 0;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                out.closeEntry();
                in.close();
            }
        }

        out.close();
    }
}
