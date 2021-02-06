package org.bohr.gui;

import java.time.Duration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bohr.core.SyncManager;
import org.bohr.gui.StatusBar.SyncProgressFormatter;
import org.bohr.gui.laf.BlueProgressBarUI;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.uiUtils.ColorUtils;
import org.bohr.message.GuiMessages;

public class StatusBarNew extends JPanel {
	private static final long serialVersionUID = 1L;

	private JLabel progressLabel;

	private final JProgressBar syncProgressBar = new JProgressBar();

	public StatusBarNew() {
		setOpaque(false);
		double[] colSize = { TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 20 };
		double[] rowSize = { TableLayout.PREFERRED, 14, 8 };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);

		setLayout(layout);

		JLabel sysLabel = new JLabel(GuiMessages.get("SyncProgress"));
		sysLabel.setFont(sysLabel.getFont().deriveFont(12F));
		sysLabel.setForeground(ColorUtils.createColor("A5AAB6"));
		add(sysLabel, "0,0");

		progressLabel = new JLabel("0.0%");
		progressLabel.setFont(progressLabel.getFont().deriveFont(12F));
		progressLabel.setForeground(ColorUtils.createColor("ffad00"));
		add(progressLabel, "2,0");

		add(syncProgressBar, "0,2,2,2");

		syncProgressBar.setMaximum(10000);

		syncProgressBar.setUI(new BlueProgressBarUI());
	}

	public void setProgress(SyncManager.Progress progress) {
		int n = (int) Math.round((double) progress.getCurrentHeight() / (double) progress.getTargetHeight() * 10000d);
		syncProgressBar.setValue(n);

		String info = "";
		Duration estimation = progress.getSyncEstimation();
		if (estimation != null && estimation.getSeconds() > 0L) {
			String timeStr = DurationFormatUtils.formatDurationWords(estimation.toMillis(), true, true);
			timeStr = timeStr.replaceAll("minutes","mins");
			timeStr = timeStr.replaceAll("seconds","secs");
			if(timeStr.indexOf("days") != -1){
				timeStr = timeStr.substring(0,timeStr.indexOf("hours")+5);
			}else if(timeStr.indexOf("hours") != -1){
				timeStr = timeStr.substring(0,timeStr.indexOf("mins")+4);
			}
			info = String.format("%s (%s)", SyncProgressFormatter.format(progress),
					estimation.toDays() >= 30 ? ">= 1 month"
							: timeStr);
		} else {
			info = SyncProgressFormatter.format(progress);
		}

		progressLabel.setText(info);
	}
}
