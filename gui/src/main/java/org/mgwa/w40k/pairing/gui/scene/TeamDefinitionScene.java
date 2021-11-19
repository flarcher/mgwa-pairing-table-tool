package org.mgwa.w40k.pairing.gui.scene;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class TeamDefinitionScene extends AbstractMainScene {

	public TeamDefinitionScene(Function<String, String> labelGetter, Runnable next) {
		super(labelGetter, 3);
		this.next = next;
		this.fileName = NodeFactory.createText(labelGetter.apply("no.file.selected"));
		this.nameLabel = NodeFactory.createText(labelGetter.apply("team.name"));
		this.tableTokenLabel = NodeFactory.createText(labelGetter.apply("table.token"));
	}

	private final Runnable next;
	private final TextField yourTeamName = new TextField();
	private final TextField otherTeamName = new TextField();
	private final ToggleGroup tokenOwnership = new ToggleGroup();
	private final Text nameLabel;
	private final Text tableTokenLabel;
	private final FileChooser fileChooser = new FileChooser();
	private final Text fileName;

	private String filePath(Optional<Path> path) {
		return path.map(Path::toString).orElse(labelGetter.apply("no.file.selected"));
	}

	@Override
	protected void buildScene(AppState state, Stage stage) {

		addNode(nameLabel, getRowIndex(), 1, 1, 1, HPos.CENTER);
		addNode(tableTokenLabel, getRowIndex(), 2, 1, 1, HPos.CENTER);
		newRow();

		RadioButton youHaveTheToken = new RadioButton();
		youHaveTheToken.setToggleGroup(tokenOwnership);
		youHaveTheToken.setSelected(state.youHaveTheTableToken());
		youHaveTheToken.setAlignment(Pos.CENTER);
		//youHaveTheToken.setMinWidth(100.0);
		yourTeamName.setText(state.getRowTeamName());
		addRow(HPos.CENTER,
			NodeFactory.createLabel(labelGetter.apply("team.yours"), yourTeamName),
			yourTeamName, youHaveTheToken);

		RadioButton theyHaveTheToken = new RadioButton();
		theyHaveTheToken.setToggleGroup(tokenOwnership);
		theyHaveTheToken.setSelected(! state.youHaveTheTableToken());
		theyHaveTheToken.setAlignment(Pos.CENTER);
		//theyHaveTheToken.setMinWidth(100.0);
		otherTeamName.setText(state.getColTeamName());
		addRow(HPos.CENTER,
			NodeFactory.createLabel(labelGetter.apply("team.other"), otherTeamName),
			otherTeamName, theyHaveTheToken);

		/*tokenOwnership.selectedToggleProperty().addListener(
			(ObservableValue<? extends Toggle> ov,
			 Toggle old_toggle, Toggle new_toggle) -> { 	});*/

		newRow();
		String selectYourFile = labelGetter.apply("select.matrix.file");
		fileChooser.setTitle(selectYourFile);
		Button fileSelectButton = NodeFactory.createButton(selectYourFile, e -> {
				@Nullable File file = fileChooser.showOpenDialog(stage);
				Optional<Path> path = Optional.ofNullable(file).map(File::toPath);
				state.setMatrixFilePath(path.orElse(null));
				fileName.setText(filePath(path));
			});
		addNode(fileSelectButton, getRowIndex(), 0, 1, 1, HPos.LEFT);
		fileName.setText(filePath(state.getMatrixFilePath()));
		addNode(fileName, getRowIndex(), 1, 1, 2, HPos.LEFT);
		newRow();

		Button nextButton = NodeFactory.createButton(labelGetter.apply("next"), e -> {
				state.setRowTeamName(yourTeamName.getText());
				state.setColTeamName(otherTeamName.getText());
				state.setYouHaveTheTableToken(tokenOwnership.getSelectedToggle() == youHaveTheToken);
				next.run();
			}  );
		addNode(nextButton, getRowIndex(), 1, 1, 2, HPos.RIGHT);
	}

}
