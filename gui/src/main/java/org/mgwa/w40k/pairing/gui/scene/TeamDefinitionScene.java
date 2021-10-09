package org.mgwa.w40k.pairing.gui.scene;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.File;
import java.util.Optional;

public class TeamDefinitionScene extends AbstractMainScene {

	public TeamDefinitionScene(Runnable next, Stage stage) {
		super(3);
		this.next = next;
		this.stage = stage;
	}

	private final Stage stage;
	private final Runnable next;
	private final TextField yourTeamName = new TextField();
	private final TextField otherTeamName = new TextField();
	private final ToggleGroup tokenOwnership = new ToggleGroup();
	private final Text nameLabel = NodeFactory.createText("Team's name");
	private final Text tableTokenLabel = NodeFactory.createText("Table token");
	private final FileChooser fileChooser = new FileChooser();

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

		fileChooser.setTitle("Select your matrix file");
		Button fileSelectButton = NodeFactory.createButton("Select your matrix file", e -> {
				File file = fileChooser.showOpenDialog(stage);
				state.setMatrixFilePath(Optional.ofNullable(file).map(File::toPath).orElse(null));
			});
		addNode(fileSelectButton, getRowIndex(), 1, 1, 1, HPos.CENTER);
		newRow();

		Button nextButton = NodeFactory.createButton("Next", e -> {
				state.setRowTeamName(yourTeamName.getText());
				state.setColTeamName(otherTeamName.getText());
				state.setYouHaveTheTableToken(tokenOwnership.getSelectedToggle() == youHaveTheToken);
				next.run();
			}  );
		addNode(nextButton, getRowIndex(), 1, 1, 2, HPos.RIGHT);
	}

}
