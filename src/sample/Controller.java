package sample;

import javafx.application.Platform;
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
    public Button reset = new Button();
    private ObservableList<Instruction> sequence =
            FXCollections.observableArrayList();
    Deque<Integer> memoryPages = new LinkedList<Integer>();
    private int allNum = 0;
    private int faultNum = 0;
    private int pageIndex = 0;
    public RunToEnd runToEnd = new RunToEnd();

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


    }
    public class RunToEnd implements  Runnable {
        @Override
        public void run() {
            while(sequence.size() != 0) {
                if (fifo.isSelected()) {
                    singleFifo();
                } else if (lru.isSelected()) {
                    singleLru();
                }
                single.setDisable(true);
                sequence.remove(0);
                table.setItems(sequence);
                try {
                    Thread.sleep(50);
                }
                catch(Exception exc){
                    System.out.println("sleep exception!!");

                }
            }


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
        //取消单选按钮
        fifo.setDisable(true);
        lru.setDisable(true);
        if(memoryPages.contains(page)) {
            allNum++;
            return;
        } else {
            faultNum++;
            allNum++;
            //Platform.runLater是为了配合执行到底时，因为runToEnd是另一个线程
            if(memoryPages.size() < 4) {
                memoryPages.offer(page);
                Platform.runLater(()-> {
                    pages[pageIndex].setText(Integer.toString(page));
                    pageIndex = (pageIndex + 1) % 4;
                });

            } else if(memoryPages.size() == 4) {
                memoryPages.poll();
                memoryPages.offer(page);
                Platform.runLater(()-> {
                    pages[pageIndex].setText(Integer.toString(page));
                    pageIndex = (pageIndex + 1) % 4;
                });
            }
        }

        Platform.runLater(()-> {
            faultNumber.setText(Integer.toString(faultNum));
            faultRate.setText(Double.toString((double)faultNum/allNum));
        });
    }
    private void singleLru() {
        int page = sequence.get(0).getPageAddress();
        //取消单选按钮
        fifo.setDisable(true);
        lru.setDisable(true);
        if(memoryPages.contains(page)) {
            allNum++;
            memoryPages.remove(page);
            memoryPages.offer(page);

        } else {
            faultNum++;
            allNum++;
            if(memoryPages.size() < 4) {
                memoryPages.offer(page);
                Platform.runLater(()-> {
                    pages[pageIndex].setText(Integer.toString(page));
                    pageIndex = (pageIndex + 1) % 4;
                });
            } else if(memoryPages.size() == 4) {
                int poll = memoryPages.poll();
                Platform.runLater(()-> {
                    for(int i = 0; i < 4;i++) {
                        if(pages[i].getText().equals(Integer.toString(poll))) {
                            pages[i].setText(Integer.toString(page));
                        }
                    }
                });
                memoryPages.offer(page);
            }

        }
        Platform.runLater(()-> {
            faultNumber.setText(Integer.toString(faultNum));
            faultRate.setText(Double.toString((double)faultNum/allNum));
        });
        System.out.println(memoryPages);
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
        end.setOnAction(e-> {
            Thread tmp = new Thread(runToEnd);
            tmp.setDaemon(true);
            tmp.start();
        });
        reset.setOnAction(e-> {
            generate.setDisable(false);
            fifo.setDisable(false);
            lru.setDisable(false);
            single.setDisable(false);
            Platform.runLater(()-> {
                for(int i = 0; i < 4;i++) {
                    pages[i].setText("-1");
                }
                faultNumber.setText("0");
                faultRate.setText("0");
            });
            pageIndex = 0;
            faultNum = 0;
            allNum = 0;
            memoryPages.clear();
            sequence.clear();
        });
    }



}
