/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author bwosi
 */
public class TelaInicial extends javax.swing.JFrame {

    /**
     * Creates new form TelaInicial
     */
    public TelaInicial() {
        initComponents();
    }

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtIp = new javax.swing.JTextField();
        lblIp = new javax.swing.JLabel();
        lblPorta = new javax.swing.JLabel();
        txtPorta = new javax.swing.JTextField();
        Enviar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtIp.setText("Digite o IP aqui");
        txtIp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIpActionPerformed(evt);
            }
        });

        lblIp.setText("Qual o IP do servidor?");

        lblPorta.setText("Qual a porta do servidor?");

        txtPorta.setText("Digite a porta aqui");
        txtPorta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPortaActionPerformed(evt);
            }
        });

        Enviar.setText("Enviar");
        Enviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EnviarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(121, 121, 121)
                        .addComponent(Enviar))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(92, 92, 92)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblIp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPorta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtIp, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPorta, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(103, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblIp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtIp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(lblPorta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPorta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(Enviar)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        txtIp.getAccessibleContext().setAccessibleName("IP");
        lblIp.getAccessibleContext().setAccessibleName("IP");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtIpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIpActionPerformed

    private void txtPortaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPortaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPortaActionPerformed

    private void EnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EnviarActionPerformed
        String ip = txtIp.getText();
        try {
            int porta = Integer.parseInt(txtPorta.getText());
            try(
                Socket socket = new Socket(ip, porta);
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                javax.swing.JOptionPane.showMessageDialog(this, "Conectado com sucesso!", "Sucesso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                
                //TODO
            } catch (IOException e ){
                javax.swing.JOptionPane.showMessageDialog(this, "Não foi possível conectar:" + e.getMessage(), "Erro de ceonexão", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e){
            javax.swing.JOptionPane.showMessageDialog(this, "Informe uma porta válida", "Erro de ceonexão", javax.swing.JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_EnviarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaInicial().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Enviar;
    private javax.swing.JLabel lblIp;
    private javax.swing.JLabel lblPorta;
    private javax.swing.JTextField txtIp;
    private javax.swing.JTextField txtPorta;
    // End of variables declaration//GEN-END:variables
}
