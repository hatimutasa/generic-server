package com.myrice.filter;

public interface IFilterChain {
	String FILTER_ACCEPT = "accept";
	String FILTER_ACCEPTOR = "acceptor";

	String FILTER_PROTOCOL_ENCODE = "encode";
	String FILTER_PROTOCOL_DECODE = "decode";
	String FILTER_MESSAGE = "message";

	String FILTER_CLOSED = "closed";

	String FILTER_ERROR = "error";

	interface FilterChain<T extends IFilter> {
		boolean hasNext();

		T nextFilter();

		FilterChain<T> getNext();
	}

	interface IChain<T extends IFilter> extends FilterChain<T> {
		T getFilter();

		IChain<T> getNext();
	}

	interface IPrevFilter {
		IFilter getFilter();
	}

	interface INextFilter {
		IFilter getFilter();
	}

	void addFirstFilter(String name, IFilter filter);

	void addLastFilter(String name, IFilter filter);

	void removeFilter(String name, IFilter filter);

	IChain<?> getFirstChain(String name);
}
