package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.util.Deque;
import java.util.LinkedList;


public class Controller {

    public AnchorPane root;
    public Button generate = new Button();
    public Button end = new Button();
    public Button single = new Button();
    public RadioButton lru = new RadioButton();
    public RadioButton fifo = new RadioButton();
    public Label faultNumber = new Label();
    public Label faultRate = new Label();
    public Label[] pages = new Label[4];
    public TableView table = new TableView();
    public TableColumn instructionCol = new TableColumn();
    public TableColumn pageAddressCol = new TableColumn();
    public ToggleGroup  algorithms = new ToggleGroup();
    private ObservableList<Instruction> sequence =
            FXCollections.observableArrayList();
    Deque<Integer> fifoMemoryPages = new LinkedList<Integer>();
    private int allNum = 0;
    private int faultNum = 0;
    private int pageIndex = 0;
    public class Instruction {
        private int instruction;
        private int pageAddress;
        public Instruction(int instruction_,int pageAddress_) {
            instruction = instruction_;
            pageAddress = pageAddress_;
        }

        public int getInstruction() {
            return instruction;
        }

        public int getPageAddress() {
            return pageAddress;
        }

        public void setInstruction(int instruction) {
            this.instruction = instruction;
        }

        public void setPageAddress(int pageAddress) {
            this.pageAddress = pageAddress;
        }
    }

    private void generateSequence() {
        int m, m1, m2;
        m = (int) (Math.random() * 320);
        sequence.add(new Instruction(m, m / 10));
        sequence.add(new Instruction(m + 1, (m + 1) / 10));
        for (int i = 0; i < 79; i++) {
            m1 = (int) (Math.random() * m);
            sequence.add(new Instruction(m1, m1 / 10));
            sequence.add(new Instruction(m1 + 1, (m1 + 1) / 10));
            m2 = m1 + 2 + (int) (Math.random() * (320 - m1 - 2));
            sequence.add(new Instruction(m2, m2 / 10));
            sequence.add(new Instruction(m2 + 1, (m2 + 1) / 10));
        }
        //还剩两条跳转到前地址部分的指令
        m1 = (int) (Math.random() * m);
        sequence.add(new Instruction(m1, m1 / 10));
        sequence.add(new Instruction(m1 + 1, (m1 + 1) / 10));
        generate.setDisable(true);
    }
    private void singleFifo() {
        int page = sequence.get(0).getPageAddress();
        //contain这里应该有问题
        if(fifoMemoryPages.contains(page)) {
            allNum++;
            return;
        } else {
            faultNum++;
            if(fifoMemoryPages.size() < 4) {
                fifoMemoryPages.offer(page);
                pages[pageIndex].setText(Integer.toString(page));
            } else if(fifoMemoryPages.size() == 4) {
                fifoMemoryPages.poll();
                pages[pageIndex].setText(Integer.toString(page));
            }
            pageIndex = (pageIndex + 1) % 4;
        }

    }
    private void singleLru() {

    }

    public void initialize() {
        for(int i = 0; i < 4;i++) {
            pages[i] = (Label)root.lookup("#page" +i);
        }
        lru.setToggleGroup(algorithms);
        fifo.setToggleGroup(algorithms);
        fifo.setSelected(true);
        instructionCol.setCellValueFactory(new PropertyValueFactory<>("instruction"));
        pageAddressCol.setCellValueFactory(new PropertyValueFactory<>("pageAddress"));
        table.setItems(sequence);
        generate.setOnAction(e-> generateSequence());
        single.setOnAction(e-> {
            if(sequence.size() != 0) {
                if (fifo.isSelected()) {
                    singleFifo();
                } else if (lru.isSelected()) {
                    singleLru();
                }
                sequence.remove(0);
                table.setItems(sequence);
            }
        });
    }



}
