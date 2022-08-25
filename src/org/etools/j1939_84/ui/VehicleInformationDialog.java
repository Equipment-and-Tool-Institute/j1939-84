/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.etools.j1939_84.utils.VinDecoder.MAX_MODEL_YEAR;
import static org.etools.j1939_84.utils.VinDecoder.MIN_MODEL_YEAR;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AbstractDocument;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.ui.widgets.LengthLimitedDocument;
import org.etools.j1939_84.ui.widgets.TextChangeListener;
import org.etools.j1939_84.ui.widgets.VinSanitizingDocumentFilter;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;

/**
 * Dialog used to collect information about the vehicle
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class VehicleInformationDialog extends JDialog implements VehicleInformationContract.View {

    private static final long serialVersionUID = -8012950984235933273L;
    private transient final VehicleInformationContract.Presenter presenter;
    private JPanel buttonPanel;
    private JSpinner calIdsSpinner;
    private JButton cancelButton;
    private JLabel certificationLabel;
    private JScrollPane certificationScrollPane;
    private JTextArea certificationTextArea;
    private JLabel emissionUnitsLabel;
    private JSpinner emissionUnitsSpinner;
    private JLabel engineModelYearLabel;
    private JSpinner engineModelYearSpinner;
    private JComboBox<FuelType> fuelTypeComboBox;
    private JLabel fuelTypeLabel;
    private JLabel headerLabel;
    private JPanel mainPanel;
    private JSpinner numberOfTripsForFaultBImplantSpinner;
    private JLabel overrideLabel;
    private JCheckBox overrideCheckBox;
    private JButton okButton;
    private JLabel vehicleModelYearLabel;
    private JSpinner vehicleModelYearSpinner;
    private JLabel vehicleModelYearValidationLabel;
    private JLabel vinLabel;
    private JCheckBox usCarbCheckBox;
    private JTextField vinTextField;
    private JLabel vinValidationLabel;

    /**
     * Constructor exposed for testing
     *
     * @param presenter
     *                      the {@link VehicleInformationContract.Presenter}
     * @param frame
     *                      main application frame
     */
    /* package */ VehicleInformationDialog(JFrame frame, VehicleInformationContract.Presenter presenter) {
        super(frame);
        this.presenter = presenter;
        initialize();
    }

    /**
     * Creates a new instance of the Dialog
     *
     * @param frame
     *                     main application frame
     * @param listener
     *                     the {@link VehicleInformationListener} that will be returned
     *                     the information entered by the user
     * @param j1939
     *                     the vehicle bus
     */
    /* package */ VehicleInformationDialog(JFrame frame, VehicleInformationListener listener, J1939 j1939) {
        super(frame);
        presenter = new VehicleInformationPresenter(this, listener, j1939);
        initialize();
    }

    /**
     * Creates and return {@link GridBagConstraints} for the column with Labels
     *
     * @param  gridy
     *                   the y coordinate in the grid
     * @return       {@link GridBagConstraints}
     */
    private static GridBagConstraints getLabelGbc(int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = gridy;
        return gbc;
    }

    private static SpinnerNumberModel getModelYearSpinnerNumberModel() {
        return new SpinnerNumberModel(MIN_MODEL_YEAR, MIN_MODEL_YEAR, MAX_MODEL_YEAR, 1);
    }

    /**
     * Creates and returns {@link GridBagConstraints} from the Validation Column
     *
     * @param  gridx
     *                   the x coordinate on the grid
     * @param  gridy
     *                   the y coordinate on the grid
     * @return       {@link GridBagConstraints}
     */
    private static GridBagConstraints getValidationGbc(int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    /**
     * Creates and returns {@link GridBagConstraints} for the Value Column
     *
     * @param  gridy
     *                   the y coordinate in the grid
     * @return       {@link GridBagConstraints}
     */
    private static GridBagConstraints getValueGbc(int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = gridy;
        return gbc;
    }

    /**
     * Creates and returns the panel which contains the OK and Cancel Button
     *
     * @return {@link JPanel}
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();

            GridBagLayout panelLayout = new GridBagLayout();
            panelLayout.columnWidths = new int[] { 0, 0 };
            panelLayout.rowHeights = new int[] { 0 };
            panelLayout.columnWeights = new double[] { 1.0, 1.0 };
            panelLayout.rowWeights = new double[] { 1.0 };
            buttonPanel.setLayout(panelLayout);

            GridBagConstraints okButtonGbc = new GridBagConstraints();
            okButtonGbc.insets = new Insets(5, 5, 5, 5);
            okButtonGbc.gridx = 0;
            okButtonGbc.gridy = 0;
            buttonPanel.add(getOkButton(), okButtonGbc);

            GridBagConstraints cancelButtonGbc = new GridBagConstraints();
            cancelButtonGbc.insets = new Insets(5, 5, 5, 5);
            cancelButtonGbc.gridx = 1;
            cancelButtonGbc.gridy = 0;
            buttonPanel.add(getCancelButton(), cancelButtonGbc);
        }
        return buttonPanel;
    }

    private JSpinner getCalIdsJSpinner() {
        if (calIdsSpinner == null) {
            calIdsSpinner = new JSpinner(new SpinnerNumberModel(99, 1, 99, 1));
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(calIdsSpinner, "#");
            editor.getTextField().setColumns(2);
            calIdsSpinner.setEditor(editor);
            calIdsSpinner
                         .addChangeListener(e -> presenter.onCalIdsChanged((int) calIdsSpinner.getValue()));
        }
        return calIdsSpinner;
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> presenter.onCancelButtonClicked());
        }
        return cancelButton;
    }

    private JLabel getCertificationLabel() {
        if (certificationLabel == null) {
            certificationLabel = new JLabel("Certification Intent");
        }
        return certificationLabel;
    }

    private JScrollPane getCertificationScrollPane() {
        if (certificationScrollPane == null) {
            certificationScrollPane = new JScrollPane(getCertificationTextField());
        }
        return certificationScrollPane;
    }

    private JTextArea getCertificationTextField() {
        if (certificationTextArea == null) {
            certificationTextArea = new JTextArea(4, 30);
            certificationTextArea.setWrapStyleWord(true);
            certificationTextArea.setLineWrap(true);
            certificationTextArea.setDocument(new LengthLimitedDocument(200));
            certificationTextArea.getDocument().addDocumentListener(new TextChangeListener() {
                @Override
                public void textChanged() {
                    presenter.onCertificationChanged(certificationTextArea.getText());
                }
            });
        }
        return certificationTextArea;
    }

    private JSpinner getEmissionUnitsJSpinner() {
        if (emissionUnitsSpinner == null) {
            emissionUnitsSpinner = new JSpinner(new SpinnerNumberModel(99, 1, 99, 1));
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(emissionUnitsSpinner, "#");
            editor.getTextField().setColumns(2);
            emissionUnitsSpinner.setEditor(editor);
            emissionUnitsSpinner
                                .addChangeListener(e -> presenter.onEmissionUnitsChanged((int) emissionUnitsSpinner.getValue()));
        }
        return emissionUnitsSpinner;
    }

    private JLabel getEmissionUnitsLabel() {
        if (emissionUnitsLabel == null) {
            emissionUnitsLabel = new JLabel("Emission Units");
        }
        return emissionUnitsLabel;
    }

    private JLabel getEngineModelYearLabel() {
        if (engineModelYearLabel == null) {
            engineModelYearLabel = new JLabel("Engine Model Year");
        }
        return engineModelYearLabel;
    }

    private JSpinner getEngineModelYearSpinner() {
        if (engineModelYearSpinner == null) {
            engineModelYearSpinner = new JSpinner(getModelYearSpinnerNumberModel());
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(engineModelYearSpinner, "#");
            editor.getTextField().setColumns(4);
            engineModelYearSpinner.setEditor(editor);
            engineModelYearSpinner.addChangeListener(
                                                     e -> presenter.onEngineModelYearChanged((int) engineModelYearSpinner.getValue()));
        }
        return engineModelYearSpinner;
    }

    private JComboBox<FuelType> getFuelTypeComboBox() {
        if (fuelTypeComboBox == null) {
            fuelTypeComboBox = new JComboBox<>();
            ComboBoxModel<FuelType> fuelTypeModel = new DefaultComboBoxModel<>(FuelType.values());
            fuelTypeComboBox.setModel(fuelTypeModel);
            fuelTypeComboBox.addActionListener(
                                               e -> presenter.onFuelTypeChanged(fuelTypeModel.getElementAt(fuelTypeComboBox.getSelectedIndex())));
        }
        return fuelTypeComboBox;
    }

    private JLabel getFuelTypeLabel() {
        if (fuelTypeLabel == null) {
            fuelTypeLabel = new JLabel("Fuel Type");
        }
        return fuelTypeLabel;
    }

    private JLabel getHeaderLabel() {
        if (headerLabel == null) {
            headerLabel = new JLabel("Please enter the vehicle information");
        }
        return headerLabel;
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            GridBagLayout panelLayout = new GridBagLayout();
            panelLayout.columnWidths = new int[] { 0, 0, 0, 0 };
            panelLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            panelLayout.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0 };
            panelLayout.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };

            mainPanel = new JPanel();
            mainPanel.setLayout(panelLayout);

            GridBagConstraints headerLabelGbc = new GridBagConstraints();
            headerLabelGbc.gridwidth = 4;
            headerLabelGbc.gridheight = 2;
            headerLabelGbc.insets = new Insets(5, 5, 5, 5);
            headerLabelGbc.gridx = 0;
            headerLabelGbc.gridy = 0;
            mainPanel.add(getHeaderLabel(), headerLabelGbc);

            mainPanel.add(getVinLabel(), getLabelGbc(2));
            GridBagConstraints vinValueGbc = getValueGbc(2);
            vinValueGbc.gridwidth = 2;
            mainPanel.add(getVinTextField(), vinValueGbc);
            mainPanel.add(getVinValidationLabel(), getValidationGbc(3, 2));

            mainPanel.add(getVehicleModelYearLabel(), getLabelGbc(3));
            mainPanel.add(getVehicleModelYearSpinner(), getValueGbc(3));
            GridBagConstraints vehicleMyValidationGbc = getValidationGbc(2, 3);
            vehicleMyValidationGbc.gridwidth = 2;
            mainPanel.add(getVehicleModelYearValidationLabel(), vehicleMyValidationGbc);

            mainPanel.add(getEngineModelYearLabel(), getLabelGbc(4));
            JPanel panel = new JPanel();
            panel.add(getEngineModelYearSpinner());
            panel.add(getUsCarbCheckBox());
            mainPanel.add(panel, getValueGbc(4));

            mainPanel.add(getFuelTypeLabel(), getLabelGbc(5));
            GridBagConstraints fuelTypeGbc = getValueGbc(5);
            fuelTypeGbc.gridwidth = 3;
            mainPanel.add(getFuelTypeComboBox(), fuelTypeGbc);

            mainPanel.add(getEmissionUnitsLabel(), getLabelGbc(6));
            mainPanel.add(getEmissionUnitsJSpinner(), getValueGbc(6));

            mainPanel.add(new JLabel("Calibration IDs"), getLabelGbc(7));
            mainPanel.add(getCalIdsJSpinner(), getValueGbc(7));

            mainPanel.add(getCertificationLabel(), getLabelGbc(8));
            GridBagConstraints certificationGbc = getValueGbc(8);
            certificationGbc.gridwidth = 3;
            mainPanel.add(getCertificationScrollPane(), certificationGbc);

            mainPanel.add(new JLabel("Number Of Trips For Fault B Implant"), getLabelGbc(9));
            mainPanel.add(getNumberOfTripsForFaultBImplantJSpinner(), getValueGbc(9));

            mainPanel.add(getOverrideLabel(), getLabelGbc(10));
            mainPanel.add(getOverrideControl(), getValueGbc(10));

            GridBagConstraints buttonPanelGbc = new GridBagConstraints();
            buttonPanelGbc.insets = new Insets(0, 0, 0, 5);
            buttonPanelGbc.anchor = GridBagConstraints.WEST;
            buttonPanelGbc.gridwidth = 2;
            buttonPanelGbc.gridx = 1;
            buttonPanelGbc.gridy = 11;
            mainPanel.add(getButtonPanel(), buttonPanelGbc);
            getRootPane().setDefaultButton(getOkButton());
        }
        return mainPanel;
    }

    private JSpinner getNumberOfTripsForFaultBImplantJSpinner() {
        if (numberOfTripsForFaultBImplantSpinner == null) {
            numberOfTripsForFaultBImplantSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(numberOfTripsForFaultBImplantSpinner, "#");
            editor.getTextField().setColumns(2);
            numberOfTripsForFaultBImplantSpinner.setEditor(editor);
            numberOfTripsForFaultBImplantSpinner
                                                .addChangeListener(e -> presenter
                                                                                 .onNumberOfTripsForFaultBImplantChanged(
                                                                                                                         (int) numberOfTripsForFaultBImplantSpinner.getValue()));
        }
        return numberOfTripsForFaultBImplantSpinner;
    }

    private JLabel getOverrideLabel() {
        if (overrideLabel == null) {
            overrideLabel = new JLabel("Override Errors");
        }
        return overrideLabel;
    }

    private JCheckBox getOverrideControl() {
        if (overrideCheckBox == null) {
            overrideCheckBox = new JCheckBox();
            overrideCheckBox.addActionListener(actionEvent -> presenter.onOverrideChanged(overrideCheckBox.isSelected()));
        }
        return overrideCheckBox;
    }

    private JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton("OK");
            okButton.setEnabled(false);
            okButton.addActionListener(e -> presenter.onOkButtonClicked());
        }
        return okButton;
    }

    private JLabel getVehicleModelYearLabel() {
        if (vehicleModelYearLabel == null) {
            vehicleModelYearLabel = new JLabel("Vehicle Model Year");
        }
        return vehicleModelYearLabel;
    }

    private JSpinner getVehicleModelYearSpinner() {
        if (vehicleModelYearSpinner == null) {
            vehicleModelYearSpinner = new JSpinner(getModelYearSpinnerNumberModel());
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(vehicleModelYearSpinner, "#");
            editor.getTextField().setColumns(4);
            vehicleModelYearSpinner.setEditor(editor);
            vehicleModelYearSpinner.addChangeListener(
                                                      e -> presenter.onVehicleModelYearChanged((int) vehicleModelYearSpinner.getValue()));
        }
        return vehicleModelYearSpinner;
    }

    private JLabel getVehicleModelYearValidationLabel() {
        if (vehicleModelYearValidationLabel == null) {
            vehicleModelYearValidationLabel = new JLabel("Vehicle MY does not match VIN");
            vehicleModelYearValidationLabel.setForeground(Color.RED);
        }
        return vehicleModelYearValidationLabel;
    }

    private JCheckBox getUsCarbCheckBox() {
        if (usCarbCheckBox == null) {
            usCarbCheckBox = new JCheckBox("US/CARB");
            usCarbCheckBox.addActionListener(e -> presenter.onUsCarb(usCarbCheckBox.isSelected()));
            usCarbCheckBox.setSelected(true);
        }
        return usCarbCheckBox;
    }

    private JLabel getVinLabel() {
        if (vinLabel == null) {
            vinLabel = new JLabel("Vehicle Identification Number");
        }
        return vinLabel;
    }

    private JTextField getVinTextField() {
        if (vinTextField == null) {
            vinTextField = new JFormattedTextField();
            vinTextField.setColumns(16);

            AbstractDocument document = (AbstractDocument) vinTextField.getDocument();
            document.setDocumentFilter(new VinSanitizingDocumentFilter());
            document.addDocumentListener(new TextChangeListener() {
                @Override
                public void textChanged() {
                    presenter.onVinChanged(vinTextField.getText());
                }
            });
        }
        return vinTextField;
    }

    private JLabel getVinValidationLabel() {
        if (vinValidationLabel == null) {
            vinValidationLabel = new JLabel("VIN is not valid");
            vinValidationLabel.setForeground(Color.RED);
        }
        return vinValidationLabel;
    }

    private void initialize() {
        getContentPane().add(getMainPanel());
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setTitle("Step 6.1.1.1.e");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                presenter.onDialogClosed();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void setCalIds(int calIds) {
        getCalIdsJSpinner().setValue(calIds);
    }

    @Override
    public void setCertificationIntent(String certificationIntent) {
        getCertificationTextField().setText(certificationIntent);
    }

    @Override
    public void setEmissionUnits(int count) {
        getEmissionUnitsJSpinner().setValue(count);
    }

    @Override
    public void setEngineModelYear(int modelYear) {
        getEngineModelYearSpinner().setValue(modelYear);
    }

    @Override
    public void setFuelType(FuelType fuelType) {
        getFuelTypeComboBox().setSelectedItem(fuelType);
    }

    @Override
    public void setNumberOfTripsForFaultBImplant(int count) {
        getNumberOfTripsForFaultBImplantJSpinner().setValue(count);
    }

    @Override
    public void setOkButtonEnabled(boolean isEnabled) {
        getOkButton().setEnabled(isEnabled);
        if (isEnabled && J1939_84.isAutoMode()) {
            presenter.onOkButtonClicked();
        }
    }

    @Override
    public void setVehicleModelYear(int modelYear) {
        getVehicleModelYearSpinner().setValue(modelYear);
    }

    @Override
    public void setVehicleModelYearValid(boolean isValid) {
        getVehicleModelYearValidationLabel().setVisible(!isValid);
    }

    @Override
    public void setVin(String vin) {
        getVinTextField().setText(vin);
    }

    @Override
    public void setVinValid(boolean isValid) {
        getVinValidationLabel().setVisible(!isValid);
    }

    @Override
    public void setOverrideControlVisible(boolean isVisible) {
        getOverrideControl().setVisible(isVisible);
        getOverrideLabel().setVisible(isVisible);
        if (!isVisible) {
            getOverrideControl().setSelected(false);
        }
    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        if (isVisible) {
            new Thread(presenter::readVehicle).start();
        } else {
            super.dispose();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }
}
