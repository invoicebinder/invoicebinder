/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.invoicebinder.client.ui.pages.reports;

import com.invoicebinder.client.service.report.ReportServiceClientImpl;
import com.invoicebinder.client.ui.controller.Main;
import com.invoicebinder.shared.misc.Constants;
import com.invoicebinder.shared.model.ReportDateInfo;
import com.invoicebinder.shared.model.ReportInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

import java.util.Date;
import java.util.List;

/**
 *
 * @author mon2
 */
public class IncomeAndExpense extends Composite {
    
    private static final IncomeAndExpenseUiBinder uiBinder = GWT.create(IncomeAndExpenseUiBinder.class);
    @UiField HorizontalPanel reportDateRangePanel;
    @UiField HTMLPanel reportGraphPanel;
    private final FlowPanel chartPanel;
    private Label lblReportHeader;
    private Button btnRefresh;
    private ListBox lstMonthYear;
    
    private final ReportServiceClientImpl service;
    private final Main main;
    
    interface IncomeAndExpenseUiBinder extends UiBinder<Widget, IncomeAndExpense> {
    }
    
    public void setReportData(final List<ReportInfo> list) {
        Runnable onLoadCallback = new Runnable() {
            @Override
            public void run() {
                ColumnChart area = new ColumnChart(getDataTable(list), getOptions());
                chartPanel.clear();
                chartPanel.add(area);
                chartPanel.addStyleName("report-chart");
            }
        };
        // Load the visualization api, passing the onLoadCallback to be called when loading is done.
        VisualizationUtils.loadVisualizationApi(onLoadCallback, ColumnChart.PACKAGE);
    }
    
    public IncomeAndExpense(Main main) {
        this.main = main;
        this.service = new ReportServiceClientImpl(GWT.getModuleBaseURL() + "services/report", this.main);
        initWidget(uiBinder.createAndBindUi(this));
        chartPanel = new FlowPanel();
        reportDateRangePanel.setWidth("100%");
        reportDateRangePanel.add(createReportsDateRangeTable());
        reportGraphPanel.setWidth("100%");
        reportGraphPanel.add(chartPanel);
        service.getIncomeAndExpense(getReportDateInfo().getMonthVal(), getReportDateInfo().getYearVal(), Constants.REPORT_DATERANGE_SHOWMONTHS);
    }
    
    private Options getOptions() {
        Options options = Options.create();
        AxisOptions xAxis = AxisOptions.create();
        AxisOptions yAxis = AxisOptions.create();
        
        options.setWidth(925);
        options.setHeight(550);
        xAxis.setTitle("Months");
        yAxis.setTitle("Amount ($)");
        options.setTitle("Income and Expenses");
        return options;
    }
    
    private SelectHandler createSelectHandler(final PieChart chart) {
        return new SelectHandler() {
            @Override
            public void onSelect(SelectHandler.SelectEvent event) {
                String message = "";
                
                // May be multiple selections.
                JsArray<Selection> selections = chart.getSelections();
                
                for (int i = 0; i < selections.length(); i++) {
                    // add a new line for each selection
                    message += i == 0 ? "" : "\n";
                    
                    Selection selection = selections.get(i);
                    
                    if (selection.isCell()) {
                        // isCell() returns true if a cell has been selected.
                        
                        // getRow() returns the row number of the selected cell.
                        int row = selection.getRow();
                        // getColumn() returns the column number of the selected cell.
                        int column = selection.getColumn();
                        message += "cell " + row + ":" + column + " selected";
                    } else if (selection.isRow()) {
                        // isRow() returns true if an entire row has been selected.
                        
                        // getRow() returns the row number of the selected row.
                        int row = selection.getRow();
                        message += "row " + row + " selected";
                    } else {
                        // unreachable
                        message += "Pie chart selections should be either row selections or cell selections.";
                        message += "  Other visualizations support column selections as well.";
                    }
                }
                
                Window.alert(message);
            }
        };
    }
    
    private AbstractDataTable getDataTable(List<ReportInfo> reportData) {
        DataTable data = DataTable.create();
        String[] items = new String[] { "Income", "Expenses" };
        int counter = 0;
        
        data.addColumn(AbstractDataTable.ColumnType.STRING, "Year");
        for (String item : items) {
            data.addColumn(AbstractDataTable.ColumnType.NUMBER, item);
        }
        
        data.addRows(reportData.size());
        for (ReportInfo info:reportData) {
            data.setValue(counter, 0, info.getMonth() + "-" + info.getYear());
            data.setValue(counter, 1, Float.parseFloat(info.getField1()));
            data.setValue(counter, 2, Float.parseFloat(info.getField2()));
            counter++;
        }
        return data;
    }
    
    private FlexTable createReportsDateRangeTable() {
        FlexTable table = new FlexTable();
        lblReportHeader = new Label();
        HorizontalPanel panel = new HorizontalPanel();
        DateTimeFormat dateFormat = DateTimeFormat.getFormat("MM-yyyy");
        Date date = new Date();
        btnRefresh = new Button();
        lstMonthYear = new ListBox();
        lstMonthYear.setStyleName("list-box");
        lstMonthYear.addChangeHandler(new DateChangeHandler());
        
        CalendarUtil.addMonthsToDate(date, -(Constants.REPORT_DATERANGE_SHOWMONTHS - 1));
        for (int i=0;i<Constants.REPORT_DATERANGE_SHOWMONTHS;i++) {
            lstMonthYear.addItem(dateFormat.format(date));
            CalendarUtil.addMonthsToDate(date, 1);
        }
        
        lblReportHeader.getElement().setInnerHTML("<h6><span>Reports > Income/Expenses</span></h6>");
        //create buttons panel.
        btnRefresh.setText("Refresh");
        btnRefresh.setStyleName("appbutton-default");
        panel.add(lstMonthYear);
        panel.add(btnRefresh);
        table.setCellSpacing(Constants.PANEL_CELL_SPACING);
        table.setWidget(0, 0, lblReportHeader);
        table.setWidget(0, 1, panel);
        table.getFlexCellFormatter().setWidth(0,0,"50%");
        return table;
    }
    
    private ReportDateInfo getReportDateInfo() {
        ReportDateInfo info = new ReportDateInfo();
        String selectedDate;
        selectedDate = lstMonthYear.getValue(lstMonthYear.getSelectedIndex());
        info.setMonthVal(Integer.parseInt(selectedDate.split("-")[0]));
        info.setYearVal(Integer.parseInt(selectedDate.split("-")[1]));
        return info;
    }
    
    private class DateChangeHandler implements ChangeHandler {
        @Override
        public void onChange(ChangeEvent event) {
            service.getIncomeAndExpense(getReportDateInfo().getMonthVal(), getReportDateInfo().getYearVal(), Constants.REPORT_DATERANGE_SHOWMONTHS);
        }
    }
}
