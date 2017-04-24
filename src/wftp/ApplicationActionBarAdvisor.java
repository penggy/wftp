package wftp;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction preferenceAction;
	private IContributionItem showViewList;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {

		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setText("退出(&X)");
		exitAction.setImageDescriptor(ImageContext
				.getImageDescriptor(ImageContext.EXIT));
		register(exitAction);
		
		
		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setText("关于(&A)");
		register(aboutAction);

		preferenceAction = ActionFactory.PREFERENCES.create(window);
		preferenceAction.setText("首选项(&P)");
		register(preferenceAction);
		
		showViewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);

	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("文件(&F)",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(exitAction);

		MenuManager windowMenu = new MenuManager("窗口(&W)",
				IWorkbenchActionConstants.M_WINDOW);
		menuBar.add(windowMenu);
		MenuManager showViewMenu = new MenuManager("显示视图(&V)",
				IWorkbenchActionConstants.SHOW_EXT);

		showViewMenu.add(showViewList);
		windowMenu.add(showViewMenu);
		windowMenu.add(preferenceAction);

		MenuManager helpMenu = new MenuManager("帮助(&H)",
				IWorkbenchActionConstants.M_HELP);
		menuBar.add(helpMenu);
		helpMenu.add(aboutAction);

	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		super.fillCoolBar(coolBar);
		// IToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT
		// | SWT.RIGHT);
		// coolBar.add(toolBarManager);
		// toolBarManager.add(exitAction);
		// //如果要显示图像和文字,用ActionContributionItem包装
	}

}
