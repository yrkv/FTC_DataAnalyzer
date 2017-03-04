package ftc.enums;

public enum Button {
	MAIN_MENU (0, 0, 100, 20, MenuState.MAIN_MENU),
	GRAPHS 	  (0, 20, 100, 20, MenuState.GRAPHS),
	MAP 	  (0, 40, 100, 20, MenuState.MAP),
	OVERVIEW  (0, 60, 100, 20, MenuState.OVERVIEW);

	public int x, y, width, height;
	public MenuState menuState;

	Button(int x, int y, int width, int height, MenuState menuState) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.menuState = menuState;
	}
}