package org.bohr.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TableLayout implements LayoutManager2, Serializable {
	private static final long serialVersionUID = 1L;

	public static final int LEFT = 0;

	public static final int RIGHT = 3;

	public static final int TOP = 0;

	public static final int BOTTOM = 3;

	public static final int CENTER = 1;

	public static final int FULL = 2;

	public static final double FILL = -1.0;

	public static final double PREFERRED = -2.0;

	public static final double MINIMUM = -3.0;

	private double[] colSize;

	private double[] rowSize;

	private Cell[][] cells;

	/**
	 * double[] colSize={FILL,PREFERRED,MINIMUM,100,0.5};<br>
	 * double[] rowSize={FILL,PREFERRED,MINIMUM,100,0.5};<br>
	 * double[][] tableSize={colSize,rowSize};<br>
	 * AitTableLayout layout = new AitTableLayout(tableSize);<br>
	 * <br>
	 * JPanel panel = new JPanel();<br>
	 * panel.setLayout(layout);<br>
	 * 
	 * @param size
	 */
	public TableLayout(double[][] size) {
		if (size == null || size.length != 2) {
			double tempCol[] = { FILL };
			double tempRow[] = { FILL };
			size = new double[][] { tempCol, tempRow };
		}

		double tempCol[] = size[0];
		double tempRow[] = size[1];

		colSize = new double[tempCol.length];
		rowSize = new double[tempRow.length];
		System.arraycopy(tempCol, 0, colSize, 0, colSize.length);
		System.arraycopy(tempRow, 0, rowSize, 0, rowSize.length);

		for (int i = 0; i < colSize.length; i++) {
			if ((colSize[i] < 0.0) && (colSize[i] != FILL) && (colSize[i] != PREFERRED) && (colSize[i] != MINIMUM)) {
				colSize[i] = FILL;
			}
		}

		for (int i = 0; i < rowSize.length; i++) {
			if ((rowSize[i] < 0.0) && (rowSize[i] != FILL) && (rowSize[i] != PREFERRED) && (rowSize[i] != MINIMUM)) {
				rowSize[i] = FILL;
			}
		}

		cells = new Cell[rowSize.length][colSize.length];
		for (int row = 0; row < rowSize.length; row++) {
			for (int col = 0; col < colSize.length; col++) {
				Cell cell = new Cell();

				cell.rowSize = rowSize[row];
				cell.colSize = colSize[col];

				cell.row = row;
				cell.col = col;

				cells[row][col] = cell;
			}
		}
	}

	public void addLayoutComponent(String name, Component component) {
		addLayoutComponent(component, name);
	}

	public void addLayoutComponent(Component component, Object constraint) {
		if (component == null) {
			throw new IllegalArgumentException("error ！");
		}

		synchronized (component.getTreeLock()) {
			if ((component instanceof Component) == false) {
				throw new IllegalArgumentException("error！");
			}

			if (constraint == null) {
				throw new IllegalArgumentException("error！");
			}

			if ((constraint instanceof String) == false) {
				throw new IllegalArgumentException("error！");
			}

			String parameter = (String) constraint;

			Constraints constraints = new Constraints(component, parameter);

			int col1 = constraints.col1;
			int row1 = constraints.row1;
			int col2 = constraints.col2;
			int row2 = constraints.row2;

			int maxColIndex = colSize.length - 1;
			int maxRowIndex = rowSize.length - 1;

			if (col1 < 0) {
				throw new IllegalArgumentException("error！");
			}

			if (col2 < 0) {
				throw new IllegalArgumentException("error！");
			}

			if (row1 < 0) {
				throw new IllegalArgumentException("error！");
			}

			if (row2 < 0) {
				throw new IllegalArgumentException("error！");
			}

			if (col1 > maxColIndex) {
				throw new IllegalArgumentException("error！");
			}

			if (col2 > maxColIndex) {
				throw new IllegalArgumentException("error！");
			}

			if (row1 > maxRowIndex) {
				throw new IllegalArgumentException("error！");
			}

			if (row2 > maxRowIndex) {
				throw new IllegalArgumentException("error！");
			}

			synchronized (cells) {
				for (int row = row1; row <= row2; row++) {
					for (int col = col1; col <= col2; col++) {
						cells[row][col].list.add(constraints);
					}
				}
			}
		}
	}

	public void removeLayoutComponent(Component component) {
		synchronized (component.getTreeLock()) {
			synchronized (cells) {
				for (int row = 0; row < cells.length; row++) {
					for (int col = 0; col < cells[row].length; col++) {
						Cell cell = cells[row][col];
						List<Constraints> list = cell.list;

						for (int i = list.size() - 1; i >= 0; i--) {
							Constraints con = list.get(i);

							if (con.component.equals(component)) {
								list.remove(i);
							}
						}
					}
				}
			}
		}
	}

	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			synchronized (cells) {
				Insets insets = parent.getInsets();

				int w = parent.getWidth() - insets.left - insets.right;
				int h = parent.getHeight() - insets.top - insets.bottom;

				claCells();

				int[] tempColSize = calColSize(w);
				int[] tempRowSize = calRowSize(h);

				int totalWidth = 0;
				int totalHeight = 0;

				for (int i = 0; i < tempColSize.length; i++) {
					totalWidth = totalWidth + tempColSize[i];
				}

				for (int i = 0; i < tempRowSize.length; i++) {
					totalHeight = totalHeight + tempRowSize[i];
				}

				return new Dimension(insets.left + totalWidth + insets.right, insets.top + totalHeight + insets.bottom);
			}
		}
	}

	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			synchronized (cells) {
				Insets insets = parent.getInsets();

				int x = insets.left;
				int y = insets.top;
				int w = parent.getWidth() - insets.left - insets.right;
				int h = parent.getHeight() - insets.top - insets.bottom;

				claCells();

				int[] tempColSize = calColSize(w);
				int[] tempRowSize = calRowSize(h);

				calCellsSize(x, y, tempColSize, tempRowSize);

				resetConstraints();

				setConstraints(tempColSize, tempRowSize);
			}
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			synchronized (cells) {
				Insets insets = parent.getInsets();

				int w = parent.getWidth() - insets.left - insets.right;
				int h = parent.getHeight() - insets.top - insets.bottom;

				claCells();

				int[] tempColSize = calMinColSize(w);
				int[] tempRowSize = calMinRowSize(h);

				int totalWidth = 0;
				int totalHeight = 0;

				for (int i = 0; i < tempColSize.length; i++) {
					totalWidth = totalWidth + tempColSize[i];
				}

				for (int i = 0; i < tempRowSize.length; i++) {
					totalHeight = totalHeight + tempRowSize[i];
				}

				return new Dimension(insets.left + totalWidth + insets.right, insets.top + totalHeight + insets.bottom);
			}
		}
	}

	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}

	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}

	public void invalidateLayout(Container target) {
	}

	private void claCells() {
		for (int row = 0; row < cells.length; row++) {
			for (int col = 0; col < cells[row].length; col++) {
				Cell cell = cells[row][col];
				cell.calPreferred();
				cell.calMinimum();
				cell.calMaximum();
			}
		}
	}

	private int[] calColSize(int width) {

		int fillSpace = width;

		// colSize FILL,PREFERRED,MINIMUM,100,0.5
		int[] col = new int[colSize.length];

		// PREFERRED，MINIMUM，100
		for (int i = 0; i < col.length; i++) {
			if (colSize[i] == PREFERRED) {
				int w = 0;
				for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
					Cell data = cells[rowIndex][i];

					if (data.preferredWidth > w) {
						w = data.preferredWidth;
					}
				}
				col[i] = w;
				fillSpace = fillSpace - col[i];
			} else if (colSize[i] == MINIMUM) {
				int w = 0;
				for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
					Cell data = cells[rowIndex][i];

					if (data.minimumWidth > w) {
						w = data.minimumWidth;
					}
				}
				col[i] = w;
				fillSpace = fillSpace - col[i];
			} else if (colSize[i] >= 1) {

				col[i] = (int) colSize[i];
				fillSpace = fillSpace - col[i];
			}
		}

		if (fillSpace > 0) {

			double relative = 0;
			for (int i = 0; i < col.length; i++) {
				if (colSize[i] > 0 && colSize[i] < 1) {
					relative = relative + colSize[i];
				}
			}

			double totalRelative = 1;

			if (relative >= 1) {
				totalRelative = relative;
			}

			int tempWidth = fillSpace;
			for (int i = 0; i < col.length; i++) {
				if (colSize[i] > 0 && colSize[i] < 1) {
					double tempW = colSize[i] * tempWidth / totalRelative;
					col[i] = (int) tempW;
					fillSpace = fillSpace - col[i];
				}
			}
		}

		if (fillSpace > 0) {
			int fillCount = 0;
			for (int i = 0; i < col.length; i++) {
				if (colSize[i] == FILL) {
					fillCount++;
				}
			}

			if (fillCount > 0) {
				int space = Math.round(fillSpace / fillCount);
				for (int i = 0; i < col.length; i++) {
					if (colSize[i] == FILL) {
						col[i] = space;
					}
				}
			}
		}

		return col;
	}

	private int[] calRowSize(int height) {
		int fillSpace = height;

		// rowSize FILL,PREFERRED,MINIMUM,100,0.5
		int[] row = new int[rowSize.length];

		for (int i = 0; i < row.length; i++) {
			if (rowSize[i] == PREFERRED) {
				int h = 0;
				for (int colIndex = 0; colIndex < cells[i].length; colIndex++) {
					Cell data = cells[i][colIndex];

					if (data.preferredHeight > h) {
						h = data.preferredHeight;
					}
				}
				row[i] = h;
				fillSpace = fillSpace - row[i];
			} else if (rowSize[i] == MINIMUM) {
				int h = 0;
				for (int colIndex = 0; colIndex < cells[i].length; colIndex++) {
					Cell data = cells[i][colIndex];

					if (data.minimumHeight > h) {
						h = data.minimumHeight;
					}
				}
				row[i] = h;
				fillSpace = fillSpace - row[i];
			} else if (rowSize[i] >= 1) {
				row[i] = (int) rowSize[i];
				fillSpace = fillSpace - row[i];
			}
		}

		if (fillSpace > 0) {
			double relative = 0;
			for (int i = 0; i < row.length; i++) {
				if (rowSize[i] > 0 && rowSize[i] < 1) {
					relative = relative + rowSize[i];
				}
			}

			double totalRelative = 1;

			if (relative >= 1) {
				totalRelative = relative;
			}

			int tempHeight = fillSpace;
			for (int i = 0; i < row.length; i++) {
				if (rowSize[i] > 0 && rowSize[i] < 1) {
					double tempW = rowSize[i] * tempHeight / totalRelative;
					row[i] = (int) tempW;
					fillSpace = fillSpace - row[i];
				}
			}
		}

		if (fillSpace > 0) {
			int fillCount = 0;
			for (int i = 0; i < row.length; i++) {
				if (rowSize[i] == FILL) {
					fillCount++;
				}
			}

			if (fillCount > 0) {
				int space = Math.round(fillSpace / fillCount);
				for (int i = 0; i < row.length; i++) {
					if (rowSize[i] == FILL) {
						row[i] = space;
					}
				}
			}
		}

		return row;
	}

	private int[] calMinColSize(int width) {
		int[] col = new int[colSize.length];

		for (int i = 0; i < col.length; i++) {
			if (colSize[i] == PREFERRED) {
				int w = 0;
				for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
					Cell data = cells[rowIndex][i];

					if (data.preferredWidth > w) {
						w = data.preferredWidth;
					}
				}
				col[i] = w;
			} else if (colSize[i] >= 1) {
				col[i] = (int) colSize[i];
			} else {
				int w = 0;
				for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
					Cell data = cells[rowIndex][i];

					if (data.minimumWidth > w) {
						w = data.minimumWidth;
					}
				}
				col[i] = w;
			}
		}

		return col;
	}

	private int[] calMinRowSize(int height) {

		// rowSize FILL,PREFERRED,MINIMUM,100,0.5
		int[] row = new int[rowSize.length];

		for (int i = 0; i < row.length; i++) {
			if (rowSize[i] == PREFERRED) {
				int h = 0;
				for (int colIndex = 0; colIndex < cells[i].length; colIndex++) {
					Cell data = cells[i][colIndex];

					if (data.preferredHeight > h) {
						h = data.preferredHeight;
					}
				}
				row[i] = h;
			} else if (rowSize[i] >= 1) {
				row[i] = (int) rowSize[i];
			} else if (rowSize[i] == MINIMUM) {
				int h = 0;
				for (int colIndex = 0; colIndex < cells[i].length; colIndex++) {
					Cell data = cells[i][colIndex];

					if (data.minimumHeight > h) {
						h = data.minimumHeight;
					}
				}
				row[i] = h;
			}
		}

		return row;
	}

	private void calCellsSize(int x, int y, int[] tempColSize, int[] tempRowSize) {
		int tempx = x;
		int tempy = y;
		for (int row = 0; row < cells.length; row++) {
			for (int col = 0; col < cells[row].length; col++) {
				Cell cell = cells[row][col];
				cell.x = tempx;
				cell.y = tempy;
				cell.w = tempColSize[col];
				cell.h = tempRowSize[row];

				tempx = tempx + tempColSize[col];
			}
			tempx = x;
			tempy = tempy + tempRowSize[row];
		}
	}

	private void resetConstraints() {
		for (int row = 0; row < cells.length; row++) {
			for (int col = 0; col < cells[row].length; col++) {
				Cell cell = cells[row][col];
				cell.resetConstraintsList();
			}
		}
	}

	private void setConstraints(int[] tempColSize, int[] tempRowSize) {
		for (int row = 0; row < cells.length; row++) {
			for (int col = 0; col < cells[row].length; col++) {
				Cell cell = cells[row][col];

				List<Constraints> list = cell.list;
				for (Constraints con : list) {
					if (con.dirty && con.singleCell && con.component.isVisible()) {
						con.calConstraints(cell.x, cell.y, cell.w, cell.h, cell.colSize, cell.rowSize);
					}
				}
			}
		}

		for (int row = 0; row < cells.length; row++) {
			for (int col = 0; col < cells[row].length; col++) {
				Cell cell = cells[row][col];

				List<Constraints> list = cell.list;
				for (Constraints con : list) {
					if (con.dirty && con.singleCell == false && con.component.isVisible()) {
						int col1 = con.col1;
						int row1 = con.row1;
						int col2 = con.col2;
						int row2 = con.row2;

						Cell startCell = cells[row1][col1];

						int cellWidth = 0;
						int cellHeight = 0;

						for (int i = col1; i <= col2; i++) {
							int w = tempColSize[i];
							cellWidth = cellWidth + w;
						}

						for (int i = row1; i <= row2; i++) {
							int h = tempRowSize[i];
							cellHeight = cellHeight + h;
						}

						con.calConstraints(startCell.x, startCell.y, cellWidth, cellHeight, PREFERRED, PREFERRED);
					}
				}
			}
		}
	}

	private class Constraints {
		private Component component;

		private int col1;

		private int row1;

		private int col2;

		private int row2;

		private int hAlign;

		private int vAlign;

		private boolean singleCell = true;

		private boolean singleRow = true;

		private boolean singleCol = true;

		private boolean dirty = true;

		private Constraints(Component component, String parameter) {
			this.component = component;

			col1 = 0;
			row1 = 0;

			col2 = 0;
			row2 = 0;

			hAlign = FULL;
			vAlign = FULL;

			initParameter(parameter);

			if (row2 < row1) {
				row2 = row1;
			}

			if (col2 < col1) {
				col2 = col1;
			}

			if (row1 != row2) {
				singleRow = false;
			}

			if (col1 != col2) {
				singleCol = false;
			}

			if (row1 != row2 || col1 != col2) {
				singleCell = false;
			}
		}

		private void calConstraints(int x, int y, int w, int h, double colSize, double rowSize) {
			Dimension preSize = component.getPreferredSize();
			Dimension minSize = component.getMinimumSize();

			int comw = preSize.width;
			int comh = preSize.height;

			// colSize FILL,PREFERRED,MINIMUM,100,0.5
			// rowSize FILL,PREFERRED,MINIMUM,100,0.5


			if (colSize == MINIMUM) {
				comw = minSize.width;
			}

			if (rowSize == MINIMUM) {
				comh = minSize.height;
			}

			if (comw > w) {
				comw = w;
			}

			if (comh > h) {
				comh = h;
			}

			int tempx = x;
			int tempy = y;
			int tempw = w;
			int temph = h;

			// hAlign left,center,right,full
			// vAlign top,center,bottom,full

			if (hAlign == LEFT) {
				tempx = x;
				tempw = comw;
			} else if (hAlign == CENTER) {
				tempx = x + (w - comw) / 2;
				tempw = comw;
			} else if (hAlign == RIGHT) {
				tempx = x + w - comw;
				tempw = comw;
			} else if (hAlign == FULL) {
				tempx = x;
				tempw = w;
			}

			if (vAlign == TOP) {
				tempy = y;
				temph = comh;
			} else if (vAlign == CENTER) {
				tempy = y + (h - comh) / 2;
				temph = comh;
			} else if (vAlign == BOTTOM) {
				tempy = y + h - comh;
				temph = comh;
			} else if (vAlign == FULL) {
				tempy = y;
				temph = h;
			}

			component.setBounds(tempx, tempy, tempw, temph);

			dirty = false;
		}

		private void resetConstraints() {
			dirty = true;
			component.setBounds(0, 0, 0, 0);
		}

		private void initParameter(String parameter) {
			if (parameter != null) {
				String[] arr = parameter.split(",");
				if (arr != null && arr.length > 0) {
					// 1,2,2,2,C,C
					if (arr.length >= 6) {
						int col1 = new Integer(arr[0]);
						int row1 = new Integer(arr[1]);

						int col2 = new Integer(arr[2]);
						int row2 = new Integer(arr[3]);

						int hAlign = changeAnchor(arr[4]);
						int vAlign = changeAnchor(arr[5]);

						this.col1 = col1;
						this.row1 = row1;

						this.col2 = col2;
						this.row2 = row2;

						this.hAlign = hAlign;
						this.vAlign = vAlign;

						return;
					}

					// 1,2,C,C
					// 1,2,2,2
					if (arr.length >= 4) {
						if (isAllNum(arr)) {
							// 1,2,2,2
							int col1 = new Integer(arr[0]);
							int row1 = new Integer(arr[1]);

							int col2 = new Integer(arr[2]);
							int row2 = new Integer(arr[3]);

							this.col1 = col1;
							this.row1 = row1;

							this.col2 = col2;
							this.row2 = row2;

							this.hAlign = FULL;
							this.vAlign = FULL;

							return;
						} else {
							// 1,2,C,C
							int col1 = new Integer(arr[0]);
							int row1 = new Integer(arr[1]);

							int col2 = col1;
							int row2 = row1;

							int hAlign = changeAnchor(arr[2]);
							int vAlign = changeAnchor(arr[3]);

							this.col1 = col1;
							this.row1 = row1;

							this.col2 = col2;
							this.row2 = row2;

							this.hAlign = hAlign;
							this.vAlign = vAlign;

							return;
						}
					}

					// 1,2
					if (arr.length >= 2) {
						int col1 = new Integer(arr[0]);
						int row1 = new Integer(arr[1]);

						int col2 = col1;
						int row2 = row1;

						this.col1 = col1;
						this.row1 = row1;

						this.col2 = col2;
						this.row2 = row2;

						this.hAlign = FULL;
						this.vAlign = FULL;

						return;
					}
				}
			}
		}

		private int changeAnchor(String anchor) {
			if (anchor.equalsIgnoreCase("L")) {
				return LEFT;
			}

			if (anchor.equalsIgnoreCase("R")) {
				return RIGHT;
			}

			if (anchor.equalsIgnoreCase("T")) {
				return TOP;
			}

			if (anchor.equalsIgnoreCase("B")) {
				return BOTTOM;
			}

			if (anchor.equalsIgnoreCase("C")) {
				return CENTER;
			}
			return FULL;
		}

		private boolean isAllNum(String[] arr) {

			try {
				for (String s : arr) {
					new Integer(s);
				}
				return true;
			} catch (Exception e) {
			}

			return false;
		}
	}

	private class Cell {
		private int col;

		private int row;

		private double colSize;

		private double rowSize;

		private int x;

		private int y;

		private int w;

		private int h;

		private int preferredWidth;

		private int preferredHeight;

		private int minimumWidth;

		private int minimumHeight;

		private int maximumWidth;

		private int maximumHeight;

		private List<Constraints> list = new ArrayList<Constraints>();

		private void calPreferred() {
			preferredWidth = 0;
			preferredHeight = 0;
			for (Constraints con : list) {
				if (con.singleCell) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getPreferredSize();

						if (dim.width > preferredWidth) {
							preferredWidth = dim.width;
						}

						if (dim.height > preferredHeight) {
							preferredHeight = dim.height;
						}
					}
				}
				if (con.singleCell == false && con.singleRow) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getPreferredSize();

						if (dim.height > preferredHeight) {
							preferredHeight = dim.height;
						}
					}
				}
				if (con.singleCell == false && con.singleCol) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getPreferredSize();

						if (dim.width > preferredWidth) {
							preferredWidth = dim.width;
						}
					}
				}
			}
		}

		private void calMinimum() {
			minimumWidth = 0;
			minimumHeight = 0;
			for (Constraints con : list) {
				if (con.singleCell) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getMinimumSize();

						if (dim.width > minimumWidth) {
							minimumWidth = dim.width;
						}

						if (dim.height > minimumHeight) {
							minimumHeight = dim.height;
						}
					}
				}
				if (con.singleCell == false && con.singleRow) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getMinimumSize();

						if (dim.height > minimumHeight) {
							minimumHeight = dim.height;
						}
					}
				}
				if (con.singleCell == false && con.singleCol) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getMinimumSize();

						if (dim.width > minimumWidth) {
							minimumWidth = dim.width;
						}
					}
				}
			}
		}

		private void calMaximum() {
			maximumWidth = 0;
			maximumHeight = 0;
			for (Constraints con : list) {
				if (con.singleCell) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getMaximumSize();

						if (dim.width > maximumWidth) {
							maximumWidth = dim.width;
						}

						if (dim.height > maximumHeight) {
							maximumHeight = dim.height;
						}
					}
				}
				if (con.singleCell == false && con.singleRow) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getMaximumSize();

						if (dim.height > maximumHeight) {
							maximumHeight = dim.height;
						}
					}
				}
				if (con.singleCell == false && con.singleCol) {
					Component com = con.component;
					if (com.isVisible()) {
						Dimension dim = com.getMaximumSize();

						if (dim.width > maximumWidth) {
							maximumWidth = dim.width;
						}
					}
				}
			}
		}

		private void resetConstraintsList() {
			for (Constraints con : list) {
				con.resetConstraints();
			}
		}
	}
}