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
public class MacroCell extends Cell {


    public MacroCell(
                UDN udn,
                String name,
                int x, int y, int z, 
                int    numAntennasTX,
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
        
        this.type_ = UDN.CellType.MACRO;
        this.cost_ = 1000;
        this.active_ = true;
    }
    
    public MacroCell(Cell c) {
        this.id_ = c.id_;
        this.bts_ = new BTS(c.bts_);
    }
    
    @Override
    Cell newInstance() {
        return new MacroCell(this);
    }


}
