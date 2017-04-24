package wftp.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

public class ContentView extends ViewPart {

	protected TableViewer tableViewer;
	protected Combo dirCombo;
	private MenuManager menuManager;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		GridData addrData = new GridData(GridData.FILL_HORIZONTAL);
		GridData contentData = new GridData(GridData.FILL_BOTH);

		menuManager = new MenuManager();

		dirCombo = new Combo(parent, SWT.BORDER);
		dirCombo.setLayoutData(addrData);

		Composite contentPanel = new Composite(parent, SWT.BORDER);
		contentPanel.setLayoutData(contentData);
		contentPanel.setLayout(new FillLayout());
		tableViewer = new TableViewer(contentPanel, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLayout(new TableLayout());// 专用于表格的布局
	}

	// 创建表格的列
	protected TableColumn createColumn(int weight, String name) {
		Table table = tableViewer.getTable();
		TableLayout layout = (TableLayout) table.getLayout();
		layout.addColumnData(new ColumnWeightData(weight));
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(name);
		return col;
	}

	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	public void addPath(String pathname) {
		dirCombo.add(pathname, 0);
		dirCombo.select(0);
		for (int i = 1; i < dirCombo.getItemCount(); i++) {
			if (dirCombo.getItem(i).equals(pathname)) {
				dirCombo.remove(i);
			}
		}
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public void setTableViewer(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	public Combo getDirCombo() {
		return dirCombo;
	}

	public void setDirCombo(Combo dirCombo) {
		this.dirCombo = dirCombo;
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

}
