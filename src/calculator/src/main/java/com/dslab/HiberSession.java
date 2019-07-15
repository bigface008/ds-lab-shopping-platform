package com.dslab;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.File;

public class HiberSession {
    private SessionFactory sf;
    private Session s;
    private Transaction tx;
    public HiberSession(String hiberConfig) {
        sf = new Configuration().configure(new File(hiberConfig)).buildSessionFactory();
        s = sf.openSession();
    }
    public void beginTransaction() {
        tx = s.beginTransaction();
    }
    public Session session() {
        return s;
    }
    public void commit() {
        tx.commit();
    }
    public void close() {
        s.close();
        sf.close();
    }
}
