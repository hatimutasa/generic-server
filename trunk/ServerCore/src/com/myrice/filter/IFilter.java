package com.myrice.filter;

import com.myrice.filter.IFilterChain.INextFilter;
import com.myrice.filter.IFilterChain.IPrevFilter;

public interface IFilter {

	void onAdded(IFilterChain filterChain, IPrevFilter prevFilter);

	void onRemoved(IFilterChain filterChain, IPrevFilter prevFilter);

	boolean onPrevFilterAdd(IFilterChain filterChain, IPrevFilter prevFilter);

	boolean onNextFilterAdd(IFilterChain filterChain, INextFilter nextFilter);

	boolean onPrevFilterRemove(IFilterChain filterChain, IPrevFilter prevFilter);

	boolean onNextFilterRemove(IFilterChain filterChain, INextFilter nextFilter);

}
