package com.fer.hr.olap.query2.util;

import com.fer.hr.olap.dto.SaikuCube;
import com.fer.hr.olap.query2.*;
import com.fer.hr.olap.query2.ThinMeasure.Type;
import com.fer.hr.olap.query2.ThinQueryModel.AxisLocation;
import com.fer.hr.olap.query2.common.ThinQuerySet;
import com.fer.hr.olap.query2.common.ThinSortableQuerySet;
import com.fer.hr.olap.query2.common.ThinSortableQuerySet.HierarchizeMode;
import com.fer.hr.olap.query2.common.ThinSortableQuerySet.SortOrder;
import com.fer.hr.olap.query2.filter.ThinFilter;
import com.fer.hr.olap.query2.filter.ThinFilter.FilterFlavour;
import com.fer.hr.olap.query2.filter.ThinFilter.FilterFunction;
import com.fer.hr.olap.query2.filter.ThinFilter.FilterOperator;
import org.saiku.query.*;
import org.saiku.query.mdx.*;
import org.saiku.query.metadata.CalculatedMeasure;
import org.saiku.query.metadata.CalculatedMember;

import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.impl.NamedListImpl;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;

import java.util.*;

public class Thin {
	
	public static ThinQuery convert(Query query, SaikuCube cube) throws Exception {
		ThinQuery tq = new ThinQuery(query.getName(), cube);
		ThinQueryModel tqm = convert(query, tq);
		tq.setQueryModel(tqm);
		
		if (query.getParameters() != null) {
			query.retrieveParameters();
			tq.setParameters(query.getParameters());
		}
		tq.setMdx(query.getMdx());
		return tq;
	}

	private static ThinQueryModel convert(Query query, ThinQuery tq) {
		ThinQueryModel tqm = new ThinQueryModel();
		tqm.setAxes(convertAxes(query.getAxes(), tq));
		ThinDetails td = convert(query.getDetails());
		tqm.setDetails(td);
		List<ThinCalculatedMeasure> cms = convert(query.getCalculatedMeasures());
	  	List<ThinCalculatedMember> cmem = convertCM(query.getCalculatedMembers());
		tqm.setCalculatedMeasures(cms);
	  	tqm.setCalculatedMembers(cmem);
		tqm.setVisualTotals(query.isVisualTotals());
		tqm.setVisualTotalsPattern(query.getVisualTotalsPattern());

		return tqm;
	}

	private static List<ThinCalculatedMeasure> convert(List<CalculatedMeasure> qcms) {
		List<ThinCalculatedMeasure> tcms = new ArrayList<>();
		if (qcms != null && qcms.size() > 0) {
			for (CalculatedMeasure qcm : qcms) {
				ThinCalculatedMeasure tcm = new ThinCalculatedMeasure(
						qcm.getHierarchy().getUniqueName(),
						qcm.getName(), 
						qcm.getUniqueName(), 
						qcm.getCaption(), 
						qcm.getFormula(),
						qcm.getFormatProperties());
				tcms.add(tcm);
			}
		}
		
		return tcms;
	}

  private static List<ThinCalculatedMember> convertCM(List<CalculatedMember> qcms) {
	List<ThinCalculatedMember> tcms = new ArrayList<>();
	if (qcms != null && qcms.size() > 0) {
	  for (CalculatedMember qcm : qcms) {
		ThinCalculatedMember tcm = new ThinCalculatedMember(
			qcm.getHierarchy().getDimension().getName(),
			qcm.getHierarchy().getUniqueName(),
			qcm.getName(),
			qcm.getUniqueName(),
			qcm.getCaption(),
			qcm.getFormula(),
			qcm.getFormatProperties());
		tcms.add(tcm);
	  }
	}

	return tcms;
  }
	private static ThinDetails convert(QueryDetails details) {
		ThinDetails.Location location = ThinDetails.Location.valueOf(details.getLocation().toString());
		AxisLocation axis = AxisLocation.valueOf(details.getAxis().toString());
		List<ThinMeasure> measures = new ArrayList<>();
		if (details != null && details.getMeasures().size() > 0) {
			for (Measure m : details.getMeasures()) {
				ThinMeasure.Type type = Type.EXACT;
				if (m instanceof CalculatedMeasure) {
					type = Type.CALCULATED;
				}
				ThinMeasure tm = new ThinMeasure(m.getName(), m.getUniqueName(), m.getCaption(), type);
				measures.add(tm);
			}
		}
		return new ThinDetails(axis, location, measures);
	}

	private static Map<AxisLocation, ThinAxis> convertAxes(Map<Axis, QueryAxis> axes, ThinQuery tq) {
		Map<ThinQueryModel.AxisLocation, ThinAxis> thinAxes = new TreeMap<>();
		if (axes != null) {
			for (Axis axis : sortAxes(axes.keySet())) {
				if (axis != null) {
					ThinAxis ta = convertAxis(axes.get(axis), tq);
					thinAxes.put(ta.getLocation(), ta);
				}
			}
		}
		return thinAxes;
	}

	private static List<Axis> sortAxes(Set<Axis> axes) {
		List<Axis> ax = new ArrayList<>();
		for (Axis a : Axis.Standard.values()) {
			if (axes.contains(a)){
				ax.add(a);
			}
		}
		return ax;
	}
	
	private static ThinAxis convertAxis(QueryAxis queryAxis, ThinQuery tq) {
		AxisLocation loc = getLocation(queryAxis);
		List<String> aggs = queryAxis.getQuery().getAggregators(queryAxis.getLocation().toString());
		ThinAxis ta = new ThinAxis(loc, convertHierarchies(queryAxis.getQueryHierarchies(), tq), queryAxis.isNonEmpty(), aggs);
		extendSortableQuerySet(ta, queryAxis);
		return ta;
	}
	
	private static NamedList<ThinHierarchy> convertHierarchies(List<QueryHierarchy> queryHierarchies, ThinQuery tq) {
		NamedListImpl<ThinHierarchy> hs = new NamedListImpl<>();
		if (queryHierarchies != null) {
			for (QueryHierarchy qh : queryHierarchies) {
				ThinHierarchy th = convertHierarchy(qh, tq);
				hs.add(th);
			}
		}
		return hs;
	}

	private static ThinHierarchy convertHierarchy(QueryHierarchy qh, ThinQuery tq) {
	  List<String> s = new ArrayList<>();
	  for(CalculatedMember cmember: qh.getCalculatedMembers()){
		s.add(cmember.getUniqueName());
	  }
		ThinHierarchy th = new ThinHierarchy(qh.getUniqueName(), qh.getCaption(), qh.getHierarchy().getDimension()
																					.getName(), convertLevels(qh
			.getActiveQueryLevels(), tq), s);
		extendSortableQuerySet(th, qh);
		return th;
	}

	private static Map<String, ThinLevel> convertLevels(List<QueryLevel> levels, ThinQuery tq) {
		Map<String, ThinLevel> tl = new HashMap<>();
		if (levels != null) {
			for (QueryLevel ql : levels) {
				ThinLevel l = convertLevel(ql, tq);
				tl.put(ql.getName(), l);
			}
		}
		return tl;
	}

	private static ThinLevel convertLevel(QueryLevel ql, ThinQuery tq) {
		List<ThinMember> inclusions = convertMembers(ql.getInclusions());
		List<ThinMember> exclusions = convertMembers(ql.getExclusions());
		ThinMember rangeStart = convertMember(ql.getRangeStart());
		ThinMember rangeEnd = convertMember(ql.getRangeEnd());
		ThinSelection ts = new ThinSelection(ThinSelection.Type.INCLUSION, null);
		
		if (inclusions.size() > 0) {
			ts = new ThinSelection(ThinSelection.Type.INCLUSION, inclusions);
		} else if (exclusions.size() > 0) {
			ts = new ThinSelection(ThinSelection.Type.EXCLUSION, exclusions);
		} else if (rangeStart != null && rangeEnd != null){
			List<ThinMember> range = new ArrayList<>();
			range.add(rangeStart);
			range.add(rangeEnd);
			ts = new ThinSelection(ThinSelection.Type.RANGE, range);
		}
		
		if (ql.hasParameter() && ts != null) {
			ts.setParameterName(ql.getParameterName());
			tq.addParameter(ql.getParameterName());
		}
		List<String> aggs = ql.getQueryHierarchy().getQuery().getAggregators(ql.getUniqueName());
		ThinLevel l = new ThinLevel(ql.getName(), ql.getCaption(), ts, aggs);
		extendQuerySet(l, ql);
		return l;
	}

	private static List<ThinMember> convertMembers(List<Member> members) {
		List<ThinMember> ms = new ArrayList<>();
		if (members != null) {
			for (Member m : members) {
				ms.add(convertMember(m));
			}
		}
		return ms;
	}
	
	private static ThinMember convertMember(Member m) {
		if (m != null) {
			return new ThinMember(m.getName(), m.getUniqueName(), m.getCaption());
		}
		return null;
	}

	private static AxisLocation getLocation(QueryAxis axis) {
		Axis ax = axis.getLocation();
		
		if (Axis.ROWS.equals(ax)) {
			return AxisLocation.ROWS;
		} else if (Axis.COLUMNS.equals(ax)) {
			return AxisLocation.COLUMNS;
		} else if (Axis.FILTER.equals(ax)) {
			return AxisLocation.FILTER;
		} else if (Axis.PAGES.equals(ax)) {
			return AxisLocation.PAGES;
		}
		return null;
	}

	private static void extendQuerySet(ThinQuerySet ts, IQuerySet qs) {
		if (StringUtils.isNotBlank(qs.getMdxSetExpression())) {
			ts.setMdx(qs.getMdxSetExpression());
		}
		if (qs.getFilters() != null && qs.getFilters().size() > 0) {
			List<ThinFilter> filters = convertFilters(qs.getFilters());
			ts.getFilters().addAll(filters);
		}
		
	}
	
	private static List<ThinFilter> convertFilters(List<IFilterFunction> filters) {
		List<ThinFilter> tfs = new ArrayList<>();
		for (IFilterFunction f : filters) {
			if (f instanceof NameFilter) {
				NameFilter nf = (NameFilter) f;
				List<String> expressions = nf.getFilterExpression();
				expressions.add(0, nf.getHierarchy().getUniqueName());
				ThinFilter tf = new ThinFilter(FilterFlavour.Name, FilterOperator.EQUALS, FilterFunction.Filter, expressions);
				tfs.add(tf);
			}
			if (f instanceof NameLikeFilter) {
				NameLikeFilter nf = (NameLikeFilter) f;
				List<String> expressions = nf.getFilterExpression();
				expressions.add(0, nf.getHierarchy().getUniqueName());
				ThinFilter tf = new ThinFilter(FilterFlavour.NameLike, FilterOperator.LIKE, FilterFunction.Filter, expressions);
				tfs.add(tf);
			}
			if (f instanceof GenericFilter) {
				GenericFilter nf = (GenericFilter) f;
				List<String> expressions = new ArrayList<>();
				expressions.add(nf.getFilterExpression());
				ThinFilter tf = new ThinFilter(FilterFlavour.Generic, null, FilterFunction.Filter, expressions);
				tfs.add(tf);
			}
			if (f instanceof NFilter) {
				NFilter nf = (NFilter) f;
				List<String> expressions = new ArrayList<>();
				expressions.add(Integer.toString(nf.getN()));
				if (nf.getFilterExpression() != null) {
					expressions.add(nf.getFilterExpression());
				}
				FilterFunction type = FilterFunction.valueOf(nf.getFunctionType().toString());
				ThinFilter tf = new ThinFilter(FilterFlavour.N, null, type, expressions);
				tfs.add(tf);
			}
		}
		return tfs;
	}

	private static void extendSortableQuerySet(ThinSortableQuerySet ts, ISortableQuerySet qs) {
		extendQuerySet(ts, qs);
		if (qs.getHierarchizeMode() != null) {
			ts.setHierarchizeMode(HierarchizeMode.valueOf(qs.getHierarchizeMode().toString()));
		}
		if (qs.getSortOrder() != null) {
			ts.sort(SortOrder.valueOf(qs.getSortOrder().toString()), qs.getSortEvaluationLiteral());
		}
		
		
	}
	

}