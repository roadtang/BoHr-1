package org.bohr.gui.laf;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
public class AutoTextField  extends JTextField {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    protected Vector dataVector = new Vector();

    protected DocumentListener documentListener;

    protected JPopupMenu popup;

    protected JList popupList;

    protected JScrollPane scrollPane;

    public AutoTextField() {
        this(null);
    }

    @SuppressWarnings("rawtypes")
    public AutoTextField(Vector v) {
        initPopupList();
        initScrollPane();
        initPopupMenu();
        initKeyListener();
        initMouseListener();
        if (v != null && v.size() > 0) {
            for (Object o : v) {
                addElement(o);
            }
        }
    }

    protected void initPopupList() {
        popupList = new JList();
        popupList.setModel(new DefaultListModel());
        popupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (popup.isVisible()) {
                        Object o = getPopupListSelectedValue();
                        if (o != null) {
                            setText(getShowText(o));
                            popup.setVisible(false);
                        }
                        if (!AutoTextField.this.hasFocus()) {
                            AutoTextField.this.requestFocus();
                        }
                    }
                }
            }
        });
    }

    protected void initScrollPane() {
        scrollPane = new JScrollPane(popupList);
        scrollPane.setBorder(null);
    }

    protected void initPopupMenu() {
        popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.add(scrollPane, BorderLayout.CENTER);
    }

    protected void initMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && !popup.isVisible()) {
                    textChanged(e);
                }
            }
        });
    }

    protected void initKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (popup.isVisible()) {
                        if (!isPopupListSelected()) {
                            setPopupListLastOneSelected();
                        } else {
                            setPopupListSelectedIndex(getPopupListSelectedIndex() - 1);
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (popup.isVisible()) {
                        if (!isPopupListSelected()) {
                            setPopupListSelectedIndex(0);
                        } else {
                            setPopupListSelectedIndex(getPopupListSelectedIndex() + 1);
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (popup.isVisible()) {
                        Object o = getPopupListSelectedValue();
                        if (o != null) {
                            setText(getShowText(o));
                            popup.setVisible(false);
                        }
                        if (!AutoTextField.this.hasFocus()) {
                            AutoTextField.this.requestFocus();
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addAllElement(List list) {
        if (list != null && list.size() > 0) {
            dataVector.addAll(list);
        }
    }

    @SuppressWarnings("unchecked")
    public void addElement(Object value) {
        if (value != null) {
            dataVector.add(value);
        }
    }
    
    public void addItem(Object value) {
    	if (value != null) {
            dataVector.add(value);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void addElement(int index, Object value) {
        if (value != null) {
            try {
                dataVector.add(index, value);
            } catch (Exception e) {
                dataVector.add(0, value);
            }
        }
    }

    public void removeElement(Object value) {
        if (value != null) {
            dataVector.remove(value);
        }
    }

    public void removeAllElement() {
        dataVector.clear();
    }
    
    public void removeAllItems() {
    	dataVector.clear();
    }

    @Override
    public void setDocument(Document doc) {
        if (documentListener == null) {
            documentListener = createDocumentListener();
        }

        if (getDocument() != null) {
            getDocument().removeDocumentListener(documentListener);
        }

        super.setDocument(doc);

        if (doc != null) {
            doc.addDocumentListener(documentListener);
        }
    }

    protected boolean isPopupListSelected() {
        return popupList.getSelectedIndex() != -1;
    }

    protected void setPopupListSelectedIndex(int index) {
        if (popupList.getModel().getSize() <= 0) {
            return;
        }
        if (index >= popupList.getModel().getSize()) {
            index = 0;
        }
        if (index < 0) {
            index = popupList.getModel().getSize() - 1;
        }
        popupList.ensureIndexIsVisible(index);
        popupList.setSelectedIndex(index);
    }

    protected int getPopupListSelectedIndex() {
        return popupList.getSelectedIndex();
    }

    protected void setPopupListLastOneSelected() {
        int count = popupList.getModel().getSize();
        if (count > 0) {
            popupList.ensureIndexIsVisible(count - 1);
            popupList.setSelectedIndex(count - 1);
        }
    }

    protected Object getPopupListSelectedValue() {
        return popupList.getSelectedValue();
    }

    protected DocumentListener createDocumentListener() {
        return new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
//                textChanged(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
//                textChanged(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
//                textChanged(e);
            }
        };
    }

    protected String getShowText(Object o) {
        return o.toString();
    }

    protected boolean filter(String text, Object o) {
        return true;
    }

    protected void textChanged(Object even) {
        if (dataVector.size() <= 0) {
            return;
        }

        if (!isEditable() || !isEnabled() || !isFocusOwner()) {
            return;
        }

        String textContent = getText();
        ListModel listModel = popupList.getModel();
        if (listModel != null && listModel instanceof DefaultListModel) {
            DefaultListModel model = (DefaultListModel) listModel;
            model.clear();
            if (textContent == null || textContent.length() <= 0) {
                boolean mustVisible = false;
                if (even instanceof MouseEvent) {
                    MouseEvent event = (MouseEvent) even;
                    if (event.getClickCount() == 2) {
                        mustVisible = true;
                    }
                }
                if (mustVisible) {
                    for (Object o : dataVector) {
                        model.addElement(o);
                    }
                    if (model.size() > 0) {
                        setPopupListSelectedIndex(0);
                        popupList.repaint();
                        if (!popup.isVisible()) {
                            showPopup();
                        }
                    }
                } else {
                    if (popup.isVisible()) {
                        popup.setVisible(false);
                    }
                }

            } else {
                for (Object o : dataVector) {
                    if (filter(textContent, o)) {
                        model.addElement(o);
                    }
                }
                if (model.size() > 0) {
                    setPopupListSelectedIndex(0);
                    popupList.repaint();
                    if (!popup.isVisible()) {
                        showPopup();
                    }
                } else {
                    if (popup.isVisible()) {
                        popup.setVisible(false);
                    }
                }
            }
        }
        requestFocus();
    }

    protected int getPopupWidth() {
        return getWidth();
    }

    protected int getPopupHeight() {
        return 100;
    }

    protected void showPopup() {
        popup.setPopupSize(getPopupWidth(), getPopupHeight());
        if (isShowing()) {
            popup.show(this, 0, getSize().height);
        }
    }

    public void closedPopup() {
        popup.setVisible(false);
    }
    
    public boolean isPopupVisible() {
    	return popup.isVisible();
    }
    
    public void setSelectedItem(Object obj) {
    	if(obj!=null) {
    		setText(obj.toString());
    	}
    }
    
    public Object getSelectedItem() {
    	String txt = getText();
    	for(Object o:dataVector) {
    		if(o.toString().equals(txt)) {
    			return o;
    		}
    	}
    	return null;
    }
}
