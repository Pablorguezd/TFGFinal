/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.cells;

import java.util.List;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.UDN;

/**
 *
 * @author paco
 */
public class MicroCell extends Cell {

    public MicroCell(
            UDN udn,
            String name,
            int x, int y, int z,
            int numAntennasTX,
            double transmittedPower,
            double alfa,
            double beta, 
            double delta,
            double transmitterGain,
            double receptorGain,
            double workingFrequency,
            String radiationPatternFile) {
        super(udn,
                name,
                x, y, z,
                numAntennasTX,
                transmittedPower,
                alfa,
                beta,
                delta,
                transmitterGain,
                receptorGain,
                workingFrequency,
                radiationPatternFile);

        this.type_ = UDN.CellType.MICRO;
        this.cost_ = 500;
        this.active_ = false;
    }

    public MicroCell(Cell c) {
        this.id_ = c.id_;
        this.bts_ = new BTS(c.bts_);
    }

    @Override
    Cell newInstance() {
        return new MicroCell(this);
    }

}
