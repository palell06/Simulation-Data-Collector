package collector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Timer;

public class AliveMessenger extends AbstractAction {
	private static AliveMessenger _instance = new AliveMessenger();
	private final int INTERVAL = 60000;

	private AliveMessenger() {
		// Make sure update is run once before the timer starts
		update();
		// Start updating status on an INTERVAL
		new Timer(INTERVAL, (ActionListener) this).start();
	}
	
	// Returning the singleton
    public static AliveMessenger getInstance() {
        return _instance;
    }

    // Overriding for timer
	@Override
	public void actionPerformed(ActionEvent arg0) {
		update();
	}

	// Returns the current external ip determend by a 3rd party.
	private String getIp() {
		try {
			URL url = new URL("http://ip.goldclone.no/");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			return in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void update(){
		String query = "";
		if (Settings.getSimulatorID() == -1){
			// Insert
			query = "INSERT INTO simulator(" +
					"status_id, " + 
					"ip_adress, " +
					"last_seen_ts, " +
					"url) " +
					"VALUES(?,?,extract(epoch from now()),NULL) RETURNING *";
			try {
				PreparedStatement statement = Settings.getDBC().prepareStatement(query);
				statement.setInt(1, 1);
				statement.setString(2, getIp());
				ResultSet res = statement.executeQuery();
				res.next();
				Settings.setSimulatorID(res.getInt("id"));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			query = "UPDATE simulator SET " +
					"status_id = ?, " +
					"ip_adress = ?, " +
					"last_seen_ts = extract(epoch from now()), " +
					"url = NULL " + 
					"WHERE id = ?";
			try {
				PreparedStatement statement = Settings.getDBC().prepareStatement(query);
				statement.setInt(1, 1);
				statement.setString(2, getIp());
				statement.setInt(3, Settings.getSimulatorID());
				statement.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}