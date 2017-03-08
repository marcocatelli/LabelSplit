import marvin.color.MarvinColorModelConverter;
import marvin.image.MarvinImage;
import marvin.image.MarvinSegment;
import marvin.io.MarvinImageIO;
import marvin.math.MarvinMath;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static marvin.MarvinPluginCollection.floodfillSegmentation;
import static marvin.MarvinPluginCollection.morphologicalClosing;

/**
 * Created by mark on 08/03/17.
 */
public class Split {
    private JTextField txtFolder;
    private JButton btnSplit;
    private JPanel panelMain;
    private List<File> filePaths;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setContentPane(new Split().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Split() {
        btnSplit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                filePaths = new ArrayList<File>();
                final File folder = new File(txtFolder.getText());
                listFilesForFolder(folder);
                List<BufferedImage> pages = new ArrayList<BufferedImage>();
                for (File file : filePaths) {
                    if (file.getName().toLowerCase().endsWith(".pdf")) {
                        try {
                            PDDocument document = PDDocument.load(file);
                            PDFRenderer pdfRenderer = new PDFRenderer(document);
                            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300F);
                                pages.add(bim);
                            }
                            document.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                if (pages.size() > 0) {
                    for (BufferedImage page : pages) {
                        MarvinImage marvinImage = new MarvinImage(page);
                        MarvinImage original = marvinImage.clone();
                        MarvinImage bin = MarvinColorModelConverter.rgbToBinary(marvinImage, 127);
                        morphologicalClosing(bin.clone(), bin, MarvinMath.getTrueMatrix(30, 30));
                        marvinImage = MarvinColorModelConverter.binaryToRgb(bin);
                        MarvinSegment[] segments = floodfillSegmentation(marvinImage);
                        for (int i = 1; i < segments.length; i++) {
                            MarvinSegment seg = segments[i];
                            //original.drawRect(seg.x1, seg.y1, seg.width, seg.height, Color.yellow);
                            //original.drawRect(seg.x1+1, seg.y1+1, seg.width, seg.height, Color.yellow);
                            MarvinImage label = original.subimage(seg.x1, seg.y1, seg.width, seg.height);
                            MarvinImageIO.saveImage(label, "/home/mark/Documents/labels/label" + i + ".jpg");
                        }
                    }
                }
            }
        });
    }

    public void listFilesForFolder(final File folder) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filePaths.add(fileEntry);
            }
        }

    }
}