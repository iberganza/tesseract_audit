package de.vorb.tesseract.gui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import de.vorb.tesseract.gui.model.PageModel;

public class GlyphExportPane extends JPanel implements MainComponent {
    private static final long serialVersionUID = 1L;

    private final GlyphSelectionPane glyphSelectionPane;
    private final GlyphListPane glyphListPane;

    /**
     * Create the panel.
     */
    public GlyphExportPane() {
        super();
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.TRAILING);
        add(panel, BorderLayout.SOUTH);

        JButton btnExport = new JButton("Export ...");
        panel.add(btnExport);

        JSplitPane splitPane = new JSplitPane();
        add(splitPane, BorderLayout.CENTER);

        glyphSelectionPane = new GlyphSelectionPane();
        glyphListPane = new GlyphListPane();

        splitPane.setLeftComponent(glyphSelectionPane);
        splitPane.setRightComponent(glyphListPane);
    }

    public GlyphSelectionPane getGlyphSelectionPane() {
        return glyphSelectionPane;
    }

    public GlyphListPane getGlyphListPane() {
        return glyphListPane;
    }

    @Override
    public void setModel(PageModel page) {
        // TODO Auto-generated method stub
    }

    @Override
    public PageModel getModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Component asComponent() {
        return this;
    }

    // private void updateGlyphExport() {
    // final JList<Entry<String, Set<Symbol>>> glyphList =
    // exportPane.getGlyphSelectionPane().getList();
    //
    // final HashMap<String, Set<Symbol>> glyphs = new HashMap<>();
    //
    // final Page page = recognitionPane.getModel();
    //
    // // set a new renderer that has a reference to the thresholded image
    // exportPane.getGlyphListPane().getList().setCellRenderer(
    // new GlyphListCellRenderer(page.getThresholdedImage()));
    //
    // // insert all symbols into the map
    // for (final Line line : page.getLines()) {
    // for (final Word word : line.getWords()) {
    // for (final Symbol symbol : word.getSymbols()) {
    // final String sym = symbol.getText();
    //
    // if (!glyphs.containsKey(sym)) {
    // glyphs.put(sym, new TreeSet<Symbol>(symbolComparator));
    // }
    //
    // glyphs.get(sym).add(symbol);
    // }
    // }
    // }
    //
    // final LinkedList<Entry<String, Set<Symbol>>> entries = new LinkedList<>(
    // glyphs.entrySet());
    //
    // Collections.sort(entries, glyphComparator);
    //
    // final DefaultListModel<Entry<String, Set<Symbol>>> model = new
    // DefaultListModel<>();
    //
    // for (final Entry<String, Set<Symbol>> entry : entries) {
    // model.addElement(entry);
    // }
    //
    // glyphList.setModel(model);
    // }
}