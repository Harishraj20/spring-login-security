package com.login.Repository;

import javax.transaction.Transactional;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.login.Models.User;

@Repository
@Transactional
public class UserRepository {

    @Autowired
    private SessionFactory sessionFactory;

    public boolean checkExistingUser(String email) {
        try {
            Session session = sessionFactory.getCurrentSession();
            @SuppressWarnings("deprecation")
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));
            User user = (User) criteria.uniqueResult();

            return user != null;

        } catch (HibernateException e) {
            System.out.println("Hibernate Exception: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("RunTime Exception: " + e.getMessage());

            return false;
        }
    }

    public User findByEmailId(String email) {
        System.out.println("Received Email Id: "+email);
        try {
            Session session = sessionFactory.getCurrentSession();
            @SuppressWarnings("deprecation")
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));
            User user = (User) criteria.uniqueResult();
            System.out.println(user);

            return user;

        } catch (HibernateException e) {
            System.out.println("Hibernate Exception: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("RunTime Exception: " + e.getMessage());

            return null;
        }
    }

    public boolean saveUser(User user) {
        try {
            if (checkExistingUser(user.getEmail())) {
                return false;
            }

            Session session = sessionFactory.getCurrentSession();

            session.save(user);
            return true;

        } catch (HibernateException e) {
            System.out.println("Hibernate Exception: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("RunTime Exception: " + e.getMessage());
            return false;
        }
    }

}
