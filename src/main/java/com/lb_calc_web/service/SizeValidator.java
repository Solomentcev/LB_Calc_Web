package com.lb_calc_web.service;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.model.attributes.TypeLb;

import java.util.ArrayList;
import java.util.List;

public class SizeValidator {
    private final static int UPPER_FRAME_MIN=20;
    private final static int UPPER_FRAME_MAX=300;

    private final static int BOTTOM_FRAME_MIN=20;
    private final static int BOTTOM_FRAME_MAX=300;

    private final static int HEIGHT_CELL_MIN=85;
    private final static int HEIGHT_MAX=2300;

  //  private final static int HEIGHT_CELL_MAX=HEIGHT_MAX-UPPER_FRAME_MIN-BOTTOM_FRAME_MIN;
    private final static int HEIGHT_MIN=UPPER_FRAME_MIN+BOTTOM_FRAME_MIN+HEIGHT_CELL_MIN;
    private final static int HEIGHT_LC_MIN=600;
    private static final int COUNT_CELLS_MIN=1;
  //  private static final int COUNT_CELLS_MAX=(HEIGHT_MAX-UPPER_FRAME_MIN-BOTTOM_FRAME_MIN)/HEIGHT_CELL_MIN;

    private final static int WIDTH_CELL_MIN=100;
    //private final static int WIDTH_MIN=WIDTH_CELL_MIN+140;
    private final static int WIDTH_MAX=1200;
  //  private final static int WIDTH_CELL_MAX=WIDTH_MAX-WIDTH_CELL_MIN;

    private final static int DEPTH_CELL_MIN=150;
    private final static int DEPTH_MIN=DEPTH_CELL_MIN+20;
    private final static int DEPTH_MAX=900;
    private final static int DEPTH_CELL_MAX=DEPTH_MAX-20;

    public static List<String> getErrorValidateLCSizesList(LCDTO lc){
        List<String> errorList=new ArrayList<>();
        if (lc.getUpperFrame()>UPPER_FRAME_MAX) {errorList.add("Верхняя рама(%d) больше допустимой(%d-%d)".formatted(lc.getUpperFrame(),UPPER_FRAME_MIN,UPPER_FRAME_MAX));}
        if (lc.getUpperFrame()<UPPER_FRAME_MIN) {errorList.add("Верхняя рама(%d) меньше допустимой(%d-%d)".formatted(lc.getUpperFrame(),UPPER_FRAME_MIN,UPPER_FRAME_MAX));}
        if (lc.getBottomFrame()>BOTTOM_FRAME_MAX) {errorList.add("Нижняя рама(%d) больше допустимой(%d-%d)".formatted(lc.getBottomFrame(),BOTTOM_FRAME_MIN,BOTTOM_FRAME_MAX));}
        if (lc.getBottomFrame()<BOTTOM_FRAME_MIN) {errorList.add("Нижняя рама(%d) меньше допустимой(%d-%d)".formatted(lc.getBottomFrame(),BOTTOM_FRAME_MIN,BOTTOM_FRAME_MAX));}
        if (lc.getHeight()>HEIGHT_MAX){errorList.add("Высота модуля(%d) больше допустимой(%d-%d)".formatted(lc.getHeight(),HEIGHT_MIN,HEIGHT_MAX));}
        if (lc.getHeight()<HEIGHT_LC_MIN){errorList.add("Высота модуля(%d) меньше допустимой(%d-%d)".formatted(lc.getHeight(),HEIGHT_LC_MIN,HEIGHT_MAX));}
        if (lc.getDepth()>DEPTH_MAX){errorList.add("Глубина модуля(%d) больше допустимой(%d-%d)".formatted(lc.getDepth(),DEPTH_MIN,DEPTH_MAX));}
        if (lc.getDepth()<DEPTH_MIN) errorList.add("Глубина модуля(%d) меньше допустимой(%d-%d)".formatted(lc.getDepth(),DEPTH_MIN,DEPTH_MAX));
        return errorList;
    }
    public static List<String> getErrorValidateALSSizesList(ALSDTO als)  {
        List<String> errorList=new ArrayList<>();
        if (als.getUpperFrame()>UPPER_FRAME_MAX) {errorList.add("Верхняя рама(%d) больше допустимой(%d-%d)".formatted(als.getUpperFrame(),UPPER_FRAME_MIN,UPPER_FRAME_MAX));}
        if (als.getUpperFrame()<UPPER_FRAME_MIN) {errorList.add("Верхняя рама(%d) меньше допустимой(%d-%d)".formatted(als.getUpperFrame(),UPPER_FRAME_MIN,UPPER_FRAME_MAX));}
        if (als.getBottomFrame()>BOTTOM_FRAME_MAX) {errorList.add("Нижняя рама(%d) больше допустимой(%d-%d)".formatted(als.getBottomFrame(),BOTTOM_FRAME_MIN,BOTTOM_FRAME_MAX));}
        if (als.getBottomFrame()<BOTTOM_FRAME_MIN) {errorList.add("Нижняя рама(%d) меньше допустимой(%d-%d)".formatted(als.getBottomFrame(),BOTTOM_FRAME_MIN,BOTTOM_FRAME_MAX));}
        if (als.getHeight()>HEIGHT_MAX){errorList.add("Высота модуля(%d) больше допустимой(%d-%d)".formatted(als.getHeight(),HEIGHT_MIN,HEIGHT_MAX));}
        if (als.getHeight()<HEIGHT_MIN){errorList.add("Высота модуля(%d) меньше допустимой(%d-%d)".formatted(als.getHeight(),HEIGHT_MIN,HEIGHT_MAX));}

        if (als.getDepth()>DEPTH_MAX){errorList.add("Глубина модуля(%d) больше допустимой(%d-%d)".formatted(als.getDepth(),DEPTH_MIN,DEPTH_MAX));}
        if (als.getDepth()<DEPTH_MIN) errorList.add("Глубина модуля(%d) меньше допустимой(%d-%d)".formatted(als.getDepth(),DEPTH_MIN,DEPTH_MAX));
        return errorList;
    }
    public static List<List<String>> getErrorValidateLBSizesLists(ALSDTO als)  {
        List<List<String>> errorLBLists =new ArrayList<>();
        int count=0;
        for(LBDTO lb:als.getLbList()){
            List<String> errorLBList=SizeValidator.getErrorValidateLBSizesList(lb);
            if(errorLBList.isEmpty()){count++;};
            errorLBLists.add(errorLBList);
        }
        if (count==errorLBLists.size()) {
            errorLBLists.clear();
        }
        return errorLBLists;
    }
    public static List<String> getErrorValidateLBSizesList(LBDTO lb)  {
        List<String> errorList=new ArrayList<>();
        if (lb.getUpperFrame()>UPPER_FRAME_MAX) {errorList.add("Верхняя рама(%d) больше допустимой(%d-%d)".formatted(lb.getUpperFrame(),UPPER_FRAME_MIN,UPPER_FRAME_MAX));}
        if (lb.getUpperFrame()<UPPER_FRAME_MIN) {errorList.add("Верхняя рама(%d) меньше допустимой(%d-%d)".formatted(lb.getUpperFrame(),UPPER_FRAME_MIN,UPPER_FRAME_MAX));}
        if (lb.getBottomFrame()>BOTTOM_FRAME_MAX) {errorList.add("Нижняя рама(%d) больше допустимой(%d-%d)".formatted(lb.getBottomFrame(),BOTTOM_FRAME_MIN,BOTTOM_FRAME_MAX));}
        if (lb.getBottomFrame()<BOTTOM_FRAME_MIN) {errorList.add("Нижняя рама(%d) меньше допустимой(%d-%d)".formatted(lb.getBottomFrame(),BOTTOM_FRAME_MIN,BOTTOM_FRAME_MAX));}

        int heightMin=HEIGHT_CELL_MIN+lb.getUpperFrame()+lb.getBottomFrame();
        if (lb.getHeight()>HEIGHT_MAX){errorList.add("Высота модуля(%d) больше допустимой(%d-%d)".formatted(lb.getHeight(),heightMin,HEIGHT_MAX));}
        if (lb.getHeight()<heightMin){errorList.add("Высота модуля(%d) меньше допустимой(%d-%d)".formatted(lb.getHeight(),heightMin,HEIGHT_MAX));}

        if (lb.getDepth()>DEPTH_MAX){errorList.add("Глубина модуля(%d) больше допустимой(%d-%d)".formatted(lb.getDepth(),DEPTH_MIN,DEPTH_MAX));}
        if (lb.getDepth()<DEPTH_MIN) errorList.add("Глубина модуля(%d) меньше допустимой(%d-%d)".formatted(lb.getDepth(),DEPTH_MIN,DEPTH_MAX));
        int heightCellMax=lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame();
        int countCellsMax=(heightCellMax-TypeLb.valueOf(lb.getType()).getShelfThick())
                /(HEIGHT_CELL_MIN+TypeLb.valueOf(lb.getType()).getShelfThick());
        if (lb.getCountCells()<COUNT_CELLS_MIN) errorList.add("Количество ячеек (%d) меньше допустимого(%d-%d) ".formatted(lb.getCountCells(),COUNT_CELLS_MIN,countCellsMax));

        if (lb.getCountCells()>countCellsMax) errorList.add("Количество ячеек(%d) больше допустимого(%d-%d) ".formatted(lb.getCountCells(),COUNT_CELLS_MIN,countCellsMax));
        double heighCell= (double) (lb.getHeight()-lb.getUpperFrame()-lb.getBottomFrame()-(lb.getCountCells()-1)*TypeLb.valueOf(lb.getType()).getShelfThick())
                /lb.getCountCells();
        if (heighCell<HEIGHT_CELL_MIN ){errorList.add("Высота ячейки(%.2f) меньше допустимой(%d-%d)".formatted(heighCell,HEIGHT_CELL_MIN,heightCellMax));}

        int depthCell=lb.getDepth()-20;
        if (depthCell>DEPTH_CELL_MAX) errorList.add("Глубина ячейки(%d) больше допустимой(%d-%d)".formatted(depthCell,DEPTH_CELL_MIN,DEPTH_CELL_MAX));
        if (depthCell<DEPTH_CELL_MIN) errorList.add("Глубина ячейки(%d) меньше допустимой(%d-%d)".formatted(depthCell,DEPTH_CELL_MIN,DEPTH_CELL_MAX));

        int widthMin=WIDTH_CELL_MIN+TypeLb.valueOf(lb.getType()).getDeltaWidth();
        if (lb.getWidth()>WIDTH_MAX) errorList.add("Ширина модуля(%d) больше допустимой(%d-%d)".formatted(lb.getWidth(),widthMin,WIDTH_MAX));
        if (lb.getWidth()<widthMin) errorList.add("Ширина модуля(%d) меньше допустимой(%d-%d)".formatted(lb.getWidth(),widthMin,WIDTH_MAX));

        int widthCell= lb.getWidth()-TypeLb.valueOf(lb.getType()).getDeltaWidth();
        int widthCellMax=WIDTH_MAX-TypeLb.valueOf(lb.getType()).getDeltaWidth();
        if (widthCell<WIDTH_CELL_MIN) errorList.add("Ширина ячейки(%d) меньше допустимой(%d-%d)".formatted(widthCell,WIDTH_CELL_MIN,widthCellMax));
        if (widthCell>widthCellMax) errorList.add("Ширина ячейки(%d) больше допустимой(%d-%d)".formatted(widthCell,WIDTH_CELL_MIN,widthCellMax));
        return errorList;
    }
    public static List<List<List<List<String>>>> getErrorValidateProjectSizeList(ProjectDTO projectDTO){
        List<List<List<List<String>>>> errorProjectList =new ArrayList<>();
        int count=0;
        for(ALSDTO als: projectDTO.getAlsList()){

            List<String> errorALSList=getErrorValidateALSSizesList(als);
            List<String> errorLCList=getErrorValidateLCSizesList(als.getLC());
            List<List<String>> errorLBLists =getErrorValidateLBSizesLists(als);

            List<List<String>> errorALSLists=new ArrayList<>();
            List<List<String>> errorLCLists=new ArrayList<>();
            errorALSLists.add(errorALSList);
            errorLCLists.add(errorLCList);

            List<List<List<String>>> errorsALSLists=new ArrayList<>();
            errorsALSLists.add(errorALSLists);
            errorsALSLists.add(errorLCLists);
            errorsALSLists.add(errorLBLists);

            if(errorALSList.isEmpty() && errorLCList.isEmpty()&&errorLBLists.isEmpty()) count++;
            errorProjectList.add(errorsALSLists);
        }
        if (count==errorProjectList.size()){
            errorProjectList.clear();
        }
        return errorProjectList;
    }
}
