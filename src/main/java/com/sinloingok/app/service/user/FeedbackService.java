package com.sinloingok.app.service.user;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sinloingok.app.dao.FeedbackDao;
import com.sinloingok.app.models.user.Feedback;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackDao feedbackDao;

    public List<Feedback> listByUser(Long userId) {
        return feedbackDao.selectByUserId(userId);
    }

    public Feedback findById(Long id) {
        return feedbackDao.selectById(id);
    }

    public void deleteById(Long id) {
        feedbackDao.deleteById(id);
    }

    public void insert(Feedback feedback) {
        feedbackDao.insert(feedback);
    }
}
