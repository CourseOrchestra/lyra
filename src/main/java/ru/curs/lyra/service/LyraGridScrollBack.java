package ru.curs.lyra.service;

import ru.curs.lyra.dto.LyraGridAddInfo;
import ru.curs.lyra.dto.ScrollBackParams;
import ru.curs.lyra.kernel.BasicGridForm;

import java.time.LocalDateTime;

/**
 * Класс для обработки обратного движения ползунка.
 */
public final class LyraGridScrollBack implements Runnable {

    static final int DGRID_MAX_TOTALCOUNT = 50000;
    static final int DGRID_SMALLSTEP = 100;
    private static final int LYRA_SMALLFACTOR = 100;

    private final LyraService srv;

    private String dgridId;

    private BasicGridForm basicGridForm;

    private LyraGridAddInfo lyraGridAddInfo = new LyraGridAddInfo();


    LyraGridScrollBack(LyraService srv, String dGridId) {
        this.srv = srv;
        this.dgridId = dGridId;
    }

    LyraGridAddInfo getLyraGridAddInfo() {
        return lyraGridAddInfo;
    }

    void setLyraGridAddInfo(final LyraGridAddInfo aLyraGridAddInfo) {
        lyraGridAddInfo = aLyraGridAddInfo;
    }

    @SuppressWarnings("unused")
    BasicGridForm getBasicGridForm() {
        return basicGridForm;
    }

    void setBasicGridForm(final BasicGridForm aBasicGridForm) {
        basicGridForm = aBasicGridForm;
    }

    @Override
    public void run() {

        System.out.println("LyraGridScrollBack.ddddddddddddd2");
        System.out.println("className: " + basicGridForm.getClass().getSimpleName());
        System.out.println("date: " + LocalDateTime.now());
        System.out.println("lyraOldPosition: " + lyraGridAddInfo.getLyraOldPosition());
        System.out.println("lyraNewPosition: " + basicGridForm.getTopVisiblePosition());
        System.out.println("diff: "
                + (basicGridForm.getTopVisiblePosition() - lyraGridAddInfo.getLyraOldPosition()));
        System.out.println("getApproxTotalCount: " + basicGridForm.getApproxTotalCount());

        // ---------------------------------------

        int lyraApproxTotalCount = basicGridForm.getApproxTotalCount();
        if (lyraApproxTotalCount == 0) {
            return;
        }

        if ((Math.abs(basicGridForm.getTopVisiblePosition()
                - lyraGridAddInfo.getLyraOldPosition()) <= lyraApproxTotalCount / LYRA_SMALLFACTOR)
                || (basicGridForm.getApproxTotalCount() < basicGridForm.getGridHeight() * 2)) {
            lyraGridAddInfo.setLyraOldPosition(basicGridForm.getTopVisiblePosition());
            return;
        }

        int dgridNewPosition;
        if (lyraApproxTotalCount <= DGRID_MAX_TOTALCOUNT) {
            dgridNewPosition = basicGridForm.getTopVisiblePosition();
        } else {
            double d = basicGridForm.getTopVisiblePosition();
            d = (d / lyraApproxTotalCount) * lyraGridAddInfo.getDgridOldTotalCount();
            dgridNewPosition = (int) d;
        }

        lyraGridAddInfo.setLyraOldPosition(basicGridForm.getTopVisiblePosition());


        srv.sendScrollBackPosition(new ScrollBackParams(dgridId, dgridNewPosition));


    }


}
