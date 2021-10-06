package org.mgwa.w40k.pairing.gui.scene;

import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class TeamDefinitionScene extends AbstractMainScene {

	public TeamDefinitionScene(Runnable next) {
		super(3);
		this.next = next;
	}

	private final Runnable next;
	private final TextField yourTeamName = new TextField();
	private final TextField otherTeamName = new TextField();
	private final ToggleGroup tokenOwnership = new ToggleGroup();
	private final Text nameLabel = NodeFactory.createText("Team's name");
	private final Text tableTokenLabel = NodeFactory.createText("Table token");

	@Override
	protected void buildScene(AppState state) {

		addNode(nameLabel, getRowIndex(), 1, 1, 1, HPos.CENTER);
		addNode(tableTokenLabel, getRowIndex(), 2, 1, 1, HPos.CENTER);
		newRow();

		RadioButton youHaveTheToken = new RadioButton();
		youHaveTheToken.setToggleGroup(tokenOwnership);
		youHaveTheToken.setSelected(state.youHaveTheTableToken());
		youHaveTheToken.setAlignment(Pos.CENTER);
		//youHaveTheToken.setMinWidth(100.0);
		addRow(HPos.CENTER,
			NodeFactory.createLabel("Your team:", yourTeamName),
			yourTeamName, youHaveTheToken);

		RadioButton theyHaveTheToken = new RadioButton();
		theyHaveTheToken.setToggleGroup(tokenOwnership);
		theyHaveTheToken.setSelected(! state.youHaveTheTableToken());
		theyHaveTheToken.setAlignment(Pos.CENTER);
		//theyHaveTheToken.setMinWidth(100.0);
		addRow(HPos.CENTER,
			NodeFactory.createLabel("Other team:", otherTeamName),
			otherTeamName, theyHaveTheToken);

		/*tokenOwnership.selectedToggleProperty().addListener(
			(ObservableValue<? extends Toggle> ov,
			 Toggle old_toggle, Toggle new_toggle) -> { 	});*/

		Button button = NodeFactory.createButton("Define armies", e -> {
				state.setRowTeamName(yourTeamName.getText());
				state.setColTeamName(otherTeamName.getText());
				state.setYouHaveTheTableToken(tokenOwnership.getSelectedToggle() == youHaveTheToken);
				next.run();
			}  );
		addNode(button, getRowIndex(), 1, 1, 2, HPos.RIGHT);
	}

	/*
	//Creating a GridPane container
	GridPane grid = new GridPane();
grid.setPadding(new Insets(10, 10, 10, 10));
grid.setVgap(5);
grid.setHgap(5);
	//Defining the Name text field
	final TextField name = new TextField();
name.setPromptText("Enter your first name.");
name.setPrefColumnCount(10);
name.getText();
GridPane.setConstraints(name, 0, 0);
grid.getChildren().add(name);
	//Defining the Last Name text field
	final TextField lastName = new TextField();
lastName.setPromptText("Enter your last name.");
GridPane.setConstraints(lastName, 0, 1);
grid.getChildren().add(lastName);
	//Defining the Comment text field
	final TextField comment = new TextField();
comment.setPrefColumnCount(15);
comment.setPromptText("Enter your comment.");
GridPane.setConstraints(comment, 0, 2);
grid.getChildren().add(comment);
	//Defining the Submit button
	Button submit = new Button("Submit");
GridPane.setConstraints(submit, 1, 0);
grid.getChildren().add(submit);
	//Defining the Clear button
	Button clear = new Button("Clear");
GridPane.setConstraints(clear, 1, 1);
grid.getChildren().add(clear);
	 */

}
