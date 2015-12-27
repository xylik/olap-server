/*  
 *   Copyright 2012 OSBI Ltd
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.fer.hr.olap.util;

import com.fer.hr.olap.dto.SaikuCube;

import java.util.Comparator;


public class SaikuCubeCaptionComparator implements Comparator<SaikuCube> {

  public int compare( SaikuCube o1, SaikuCube o2 ) {
    if ( o1.getCaption() == null || o2.getCaption() == null ) {
      return 0;
    }
    return o1.getCaption().compareTo( o2.getCaption() );
  }
}
