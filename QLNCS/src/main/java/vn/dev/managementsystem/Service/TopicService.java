package vn.dev.managementsystem.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.dev.managementsystem.Dto.TopicDto;
import vn.dev.managementsystem.Entity.Topic;
import vn.dev.managementsystem.Entity.User;
import vn.dev.managementsystem.Enum.TopicState;
import vn.dev.managementsystem.Repository.TopicRepository;
import vn.dev.managementsystem.Repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Topic> getAllTopic(){
        return topicRepository.findAll();
    }

    public List<Topic> orderByDate(){
        return null;
    }

    public List<Topic> orderByLecturer(){
        return topicRepository.getOrderByLecturer();
    }

    public List<Topic> orderByStatus(){
        return topicRepository.getOrderByStatus();
    }

    public Topic getById(Integer id){
        return topicRepository.findById(id).orElse(null);
    }

    public Topic saveAddTopic(TopicDto dto){
        Topic entity = dtoToTopic(dto);
        if (entity == null){
            return null;
        }
        entity.setCurrentState(String.valueOf(TopicState.PENDING));
        return topicRepository.save(entity);
    }

    public Topic dtoToTopic(TopicDto dto){
        Topic add = new Topic();
        User lecturer = userRepository.findById(dto.getLecturerId()).orElse(null);
        if (lecturer == null || !lecturer.getRole().equals("lecturer")){
            return null;
        }
        add.setLecturer(lecturer);
        add.setCreate_date(LocalDate.now());
        add.setName(dto.getName());
        add.setLabel(dto.getLabel());
        add.setField(dto.getField());
        add.setPurpose(dto.getPurpose());
        add.setIdea(dto.getIdea());
        return add;
    }

    public List<Topic> orderByCreateDate() {
        return topicRepository.getOrderByCreateDate();
    }

    public Set<User> addStudentForTopic(Integer topicId, Integer userId) {
        Topic entity = topicRepository.findById(topicId).orElse(null);
        User uEn = userRepository.findById(userId).orElse(null);
        if (entity==null || uEn==null){
            return null;
        }
        entity.addRelationalStudent(uEn);
        topicRepository.save(entity);
        return entity.getStudentsOfTopic();
    }

    public Set<User> addStudentForTopic(Integer topicId, String email) {
        Topic entity = topicRepository.findById(topicId).orElse(null);
        User uEn = userRepository.getUserByName(email);
        if (entity==null || uEn==null || !uEn.getRole().equals("student")
                || uEn.getTopicOfStudent()!=null){
            return null;
        }
        entity.addRelationalStudent(uEn);
        topicRepository.save(entity);
        return entity.getStudentsOfTopic();
    }

    public Topic setToApprove(Integer id, String email) {
        Topic entity = topicRepository.findById(id).orElse(null);
        User lecturerEn = userRepository.getUserByName(email);
        if (entity == null || !lecturerEn.getRole().equals("lecturer")){
            return null;
        }
        entity.setLecturer(lecturerEn);
        entity.setPreviousState(entity.getCurrentState());
        entity.setCurrentState(String.valueOf(TopicState.APPROVED));
        return topicRepository.save(entity);
    }

    public Topic setToUnderReview(Integer id) {
        Topic entity = topicRepository.findById(id).orElse(null);
        if (entity == null){
            return null;
        }
        entity.setPreviousState(entity.getCurrentState());
        entity.setCurrentState(String.valueOf(TopicState.UNDER_REVIEW));
        return topicRepository.save(entity);
    }

    public Topic setToPauseOrResume(Integer id) {
        Topic entity = topicRepository.findById(id).orElse(null);
        if (entity == null){
            return null;
        }
        if(!entity.getCurrentState().equals("PAUSE")){
            entity.setPreviousState(entity.getCurrentState());
            entity.setCurrentState(String.valueOf(TopicState.PAUSE));
        }else {
            entity.setCurrentState(entity.getPreviousState());
        }
        return topicRepository.save(entity);
    }

    public Topic setToCancel(Integer id) {
        Topic entity = topicRepository.findById(id).orElse(null);
        if (entity == null){
            return null;
        }
        entity.setPreviousState(entity.getCurrentState());
        entity.setCurrentState(String.valueOf(TopicState.CANCEL));
        return topicRepository.save(entity);
    }

    public Topic setToScore(Integer id, String score) {
        Topic entity = topicRepository.findById(id).orElse(null);
        if (entity == null){
            return null;
        }
        entity.setScore(score);
        return topicRepository.save(entity);
    }
}
