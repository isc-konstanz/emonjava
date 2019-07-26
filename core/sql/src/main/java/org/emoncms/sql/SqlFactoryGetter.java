package org.emoncms.sql;

import org.hibernate.SessionFactory;

public interface SqlFactoryGetter {

	public SessionFactory getSessionFactory();
}
