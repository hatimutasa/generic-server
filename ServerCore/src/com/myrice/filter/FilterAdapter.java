package com.myrice.filter;

import org.apache.log4j.Logger;

import com.myrice.filter.IFilterChain.INextFilter;
import com.myrice.filter.IFilterChain.IPrevFilter;

public class FilterAdapter implements IFilter {
	protected Logger log = Logger.getLogger(getClass());

	public void onAdded(IFilterChain filterChain, IPrevFilter prevFilter) {
	}

	public void onRemoved(IFilterChain filterChain, IPrevFilter prevFilter) {
	}

	public boolean onPrevFilterAdd(IFilterChain filterChain,
			IPrevFilter prevFilter) {
		return true;
	}

	public boolean onNextFilterAdd(IFilterChain filterChain,
			INextFilter nextFilter) {
		return true;
	}

	public boolean onNextFilterRemove(IFilterChain filterChain,
			INextFilter nextFilter) {
		return true;
	}

	public boolean onPrevFilterRemove(IFilterChain filterChain,
			IPrevFilter prevFilter) {
		return true;
	}

}
