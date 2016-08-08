/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumoralgrowthautomaton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


class HistoryArray
{
    private double[] history_;
    private int   index_;
    private double min_;
    private double max_;

    HistoryArray(int size)
    {
        history_ = new double[size];
        index_   = 0;
        min_     = Double.MAX_VALUE;
        max_     = -Double.MAX_VALUE;
    }

    void push(double count)
    {
        history_[index_] = count;
        index_ = (index_ + 1) % history_.length;

        min_ = Math.min(min_, count);
        max_ = Math.max(max_, count);
    }

    double get(int position)
    {
        return history_[(index_ + position + history_.length) % history_.length];
    }

    double getMin()
    {
        return min_;
    }

    double getMax()
    {
        return max_;
    }

    double getLength()
    {
        return history_.length;
    }
}

/**
 *
 * @author manolo
 */
public class TumorAutomatonInfo extends javax.swing.JFrame {   
    BufferedImage graph;
    HistoryArray populationHistory;
    HistoryArray entropyHistory;
    /**
     * Creates new form TumoralAutomatonInfo
     */
    public TumorAutomatonInfo() {
        initComponents();
        populationHistory = new HistoryArray(1500);
        entropyHistory    = new HistoryArray(1500);
        plotPopulation.setPreferredSize(new Dimension(populationPanel.getWidth(), populationPanel.getHeight()));
        plotEntropy.setPreferredSize(new Dimension(entropyPanel.getWidth(), entropyPanel.getHeight()));
    }
    
    public void setPopulation(long count)
    {
        population.setText(String.valueOf(count));
        populationHistory.push(count);
        
        plotPopulation.paint(plotPopulation.getGraphics());
    }
    
    public void setEntropy(double count)
    {
        entropy.setText(String.valueOf(count));
        entropyHistory.push(count);
        
        plotEntropy.paint(plotEntropy.getGraphics());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        generations = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        population = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        entropy = new javax.swing.JTextField();
        plotPanel = new javax.swing.JPanel();
        populationPanel = new javax.swing.JPanel();
        plotPopulation = new JPanel()
        {
            @Override
            public void paint(Graphics g)
            {
                if (isVisible())
                {
                    graph = new BufferedImage(plotPopulation.getWidth(), plotPopulation.getHeight(), BufferedImage.TYPE_INT_RGB);

                    Graphics gImage = graph.getGraphics();
                    gImage.setColor(Color.WHITE);
                    gImage.fillRect(0, 0, plotPopulation.getWidth(), plotPopulation.getHeight());
                    gImage.setColor(Color.BLUE);

                    float step = plotPopulation.getWidth() / (float)populationHistory.getLength();
                    float yFactor = (plotPopulation.getHeight()) / (float)populationHistory.getMax();

                    for (int i = 0; i < populationHistory.getLength() - 1; ++i)
                    {
                        int x1 = (int)Math.ceil(step * i);
                        int y1 = plotPopulation.getHeight() - 1 - (int)Math.floor((populationHistory.get(i) - populationHistory.getMin()) * yFactor);
                        int x2 = (int)Math.ceil(step * (i + 1));
                        int y2 = plotPopulation.getHeight() - 1 - (int)Math.floor((populationHistory.get(i + 1) - populationHistory.getMin()) * yFactor);

                        gImage.drawLine(x1, y1, x2, y2);
                    }

                    g.drawImage(graph, 0, 0, null);
                }
            }
        };
        entropyPanel = new javax.swing.JPanel();
        plotEntropy = new JPanel()
        {
            @Override
            public void paint(Graphics g)
            {
                if (isVisible())
                {
                    graph = new BufferedImage(plotEntropy.getWidth(), plotEntropy.getHeight(), BufferedImage.TYPE_INT_RGB);

                    Graphics gImage = graph.getGraphics();
                    gImage.setColor(Color.WHITE);
                    gImage.fillRect(0, 0, plotEntropy.getWidth(), plotEntropy.getHeight());
                    gImage.setColor(Color.RED);

                    float step = plotEntropy.getWidth() / (float)entropyHistory.getLength();
                    int   yFactor = plotEntropy.getHeight();

                    double denom = entropyHistory.getMax() - entropyHistory.getMin();

                    if (denom != 0)
                    for (int i = 0; i < entropyHistory.getLength() - 1; ++i)
                    {
                        double norm1 = (entropyHistory.get(i)   - entropyHistory.getMin()) / denom;
                        double norm2 = (entropyHistory.get(i+1) - entropyHistory.getMin()) / denom;

                        int x1 = (int)Math.ceil(step * i);
                        int y1 = (int)Math.floor((1. - norm1) * yFactor);
                        int x2 = (int)Math.ceil(step * (i + 1));
                        int y2 = (int)Math.ceil(plotEntropy.getHeight() - norm2 * plotEntropy.getHeight());

                        gImage.drawLine(x1, y1, x2, y2);
                    }

                    g.drawImage(graph, 0, 0, null);
                }
            }
        };

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tumor Simulation Info");
        setMinimumSize(new java.awt.Dimension(400, 300));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jLabel1.setText("Generaciones");

        generations.setEditable(false);
        generations.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        generations.setEnabled(false);
        generations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generationsActionPerformed(evt);
            }
        });

        jLabel2.setText("Población");

        population.setEditable(false);
        population.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        population.setEnabled(false);

        jLabel3.setText("Entropía");

        entropy.setEditable(false);
        entropy.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        entropy.setEnabled(false);

        plotPanel.setLayout(new java.awt.GridLayout(2, 1));

        populationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gráfica de Crecimiento"));

        javax.swing.GroupLayout plotPopulationLayout = new javax.swing.GroupLayout(plotPopulation);
        plotPopulation.setLayout(plotPopulationLayout);
        plotPopulationLayout.setHorizontalGroup(
            plotPopulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 559, Short.MAX_VALUE)
        );
        plotPopulationLayout.setVerticalGroup(
            plotPopulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout populationPanelLayout = new javax.swing.GroupLayout(populationPanel);
        populationPanel.setLayout(populationPanelLayout);
        populationPanelLayout.setHorizontalGroup(
            populationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 559, Short.MAX_VALUE)
            .addGroup(populationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(plotPopulation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        populationPanelLayout.setVerticalGroup(
            populationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
            .addGroup(populationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(plotPopulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        plotPanel.add(populationPanel);

        entropyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gráfica de Entropía"));

        javax.swing.GroupLayout plotEntropyLayout = new javax.swing.GroupLayout(plotEntropy);
        plotEntropy.setLayout(plotEntropyLayout);
        plotEntropyLayout.setHorizontalGroup(
            plotEntropyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 559, Short.MAX_VALUE)
        );
        plotEntropyLayout.setVerticalGroup(
            plotEntropyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout entropyPanelLayout = new javax.swing.GroupLayout(entropyPanel);
        entropyPanel.setLayout(entropyPanelLayout);
        entropyPanelLayout.setHorizontalGroup(
            entropyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 559, Short.MAX_VALUE)
            .addGroup(entropyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(plotEntropy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        entropyPanelLayout.setVerticalGroup(
            entropyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
            .addGroup(entropyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(plotEntropy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        plotPanel.add(entropyPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(population)
                            .addComponent(generations)
                            .addComponent(entropy))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(generations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(population, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entropy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(plotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel1);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void generationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generationsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_generationsActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowOpened

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JTextField entropy;
    private javax.swing.JPanel entropyPanel;
    javax.swing.JTextField generations;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel plotEntropy;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JPanel plotPopulation;
    javax.swing.JTextField population;
    private javax.swing.JPanel populationPanel;
    // End of variables declaration//GEN-END:variables
}
