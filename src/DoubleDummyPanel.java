import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Read-only original-deal analysis and a comparison for the actual declarer. */
public final class DoubleDummyPanel extends JPanel {
    private final JTable table;

    /**
     * Constructs a panel showing a read-only analysis of the original deal and a comparison with the actual gameplay.
     * 
     * @param results The double dummy analysis results for the deal.
     * @param deal The deal being analyzed.
     * @param gameplay The actual play session to compare against the analysis.
     */
    public DoubleDummyPanel(DoubleDummyTable results, Deal deal, Gameplay gameplay) {
        super(new BorderLayout(8, 12));

        if (results == null || gameplay == null || !results.getHandId().equals(DoubleDummyTable.handId(deal))) {
            throw new IllegalArgumentException("Matching analysis and an existing play session are required.");
        }

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new JLabel("<html><b>Original deal: tricks with optimal play and all cards known</b><br>"
                + "Each column assumes that player declares the strain shown.</html>"), BorderLayout.NORTH);

        Object[][] rows = new Object[5][7];

        for (Suit strain : Suit.values()) {
            rows[strain.ordinal()] = new Object[] {strain == Suit.NT ? "No trump" : strain.toString(),
                results.getTricks(strain, Position.NORTH), results.getTricks(strain, Position.SOUTH),
                results.getPartnershipTricks(strain, true), results.getTricks(strain, Position.EAST),
                results.getTricks(strain, Position.WEST), results.getPartnershipTricks(strain, false)};
        }

        table = new JTable(new DefaultTableModel(rows, new String[]{"Strain", "North", "South", "NS best", "East", "West", "EW best"}) {
            /**
             * Returns false to make all table cells non-editable.
             * 
             * @param row The row index of the cell.
             * @param column The column index of the cell.
             * @return false, indicating no cell is editable.
             */
            @Override public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        });

        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setPreferredSize(new Dimension(760, 170));
        add(scroll, BorderLayout.CENTER);
        add(new JLabel("<html>NS/EW best = the better result with either partner declaring.<br>"
                + "These are separate possible contracts; the two partnership figures need not total 13.<br><br>"
                + comparison(results, gameplay)
                + "<br>Actual play may do better if the opponents make mistakes. "
                + "This is a trick benchmark, not a par contract or score.</html>"), BorderLayout.SOUTH);
    }

    /**
     * Generates a textual comparison between the double dummy benchmark and the actual declarer's tricks in the gameplay.
     * 
     * @param results The double dummy analysis results.
     * @param gameplay The actual play session.
     * @return A string describing the current or final declarer trick count compared to the double dummy benchmark.
     */
    static String comparison(DoubleDummyTable results, Gameplay gameplay) {
        Contract contract = gameplay.getContract();
        int expected = results.getTricks(contract.getDenomination(), contract.getDeclarer().getPosition());
        String prefix = "Current contract: " + contract.getLevel() + " " + contract.getDenomination()
                + "; declarer " + contract.getDeclarer() + ". Double-dummy benchmark: " + expected + " tricks.";
        if (!gameplay.isFinished()) {
            return prefix + "<br>Play in progress: declaring side has won " + gameplay.getDeclarerTricks()
                    + " completed tricks. Compare the final total when play finishes.";
        }
        int actual = gameplay.getDeclarerTricks();
        int difference = actual - expected;
        return prefix + "<br>Final declaring-side total: " + actual + " tricks ("
                + (difference > 0 ? "+" : "") + difference + " versus benchmark; includes any claim/concession allocation).";
    }

    /**
     * Returns the table component displaying the analysis results.
     * 
     * @return The JTable containing the double dummy analysis data.
     */
    JTable getResultsTable() { 
        return table; 
    }
}
