
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Created by mark on 08/03/17.
 */
public class Split {
    private JTextField txtFolder;
    private JButton btnSplit;
    private JPanel panelMain;
    private JTextField txtExtractedPages;
    private JTextField txtFragments;
    private JButton btnUnisci;
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
                    int i = 0;
                    for (BufferedImage page : pages) {
                        floodFillImage(page,0,0,Color.black);
                        try {
                            ImageIO.write(page,"PNG",new File (txtExtractedPages.getText()+i + ".png"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                }
            }
        });
        btnUnisci.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                PDDocument doc = new PDDocument();

                final File folder = new File(txtFragments.getText());
                for (File file:folder.listFiles()){
                    try {
                        BufferedImage bim = ImageIO.read(file);
                        int height = bim.getHeight();
                        int width = bim.getWidth();
                        if (width>450 && height>850){
                            PDPage page = new PDPage();
                            doc.addPage(page);
                            PDImageXObject pdImageXObject = LosslessFactory.createFromImage(doc, bim);
                            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, false);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public static void floodFillImage(BufferedImage image,int x, int y, Color color)
    {
        int srcColor = image.getRGB(x, y);
        boolean[][] hits = new boolean[image.getHeight()][image.getWidth()];

        Queue<Point> queue = new LinkedList<Point>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty())
        {
            Point p = queue.remove();

            if(floodFillImageDo(image,hits,p.x,p.y, srcColor, color.getRGB()))
            {
                queue.add(new Point(p.x,p.y - 1));
                queue.add(new Point(p.x,p.y + 1));
                queue.add(new Point(p.x - 1,p.y));
                queue.add(new Point(p.x + 1,p.y));
            }
        }
    }

    private static boolean floodFillImageDo(BufferedImage image, boolean[][] hits,int x, int y, int srcColor, int tgtColor)
    {
        if (y < 0) return false;
        if (x < 0) return false;
        if (y > image.getHeight()-1) return false;
        if (x > image.getWidth()-1) return false;

        if (hits[y][x]) return false;

        if (image.getRGB(x, y)!=srcColor)
            return false;

        // valid, paint it

        image.setRGB(x, y, tgtColor);
        hits[y][x] = true;
        return true;
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