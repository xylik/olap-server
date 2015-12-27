package com.fer.hr.olap.util;

import com.fer.hr.olap.dto.ISaikuObject;

import java.util.Comparator;

public class SaikuUniqueNameComparator implements Comparator<ISaikuObject> {

  public int compare( ISaikuObject o1, ISaikuObject o2 ) {
    return o1.getUniqueName().compareTo( o2.getUniqueName() );
  }

}
