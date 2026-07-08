package edu.cit.mahumot.daycarelog.features.children;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChildService {

    private final ChildRepository childRepository;

    public ChildService(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    public List<Child> findAll() {
        return childRepository.findAllByOrderByLastNameAsc();
    }

    public Child findById(Long id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Child not found"));
    }

    public List<Child> findByIds(List<Long> ids) {
        return childRepository.findAllById(ids);
    }

    public Child create(ChildRequest req, Long userId) {
        Child child = Child.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .dateOfBirth(req.getDateOfBirth())
                .sex(req.getSex())
                .address(req.getAddress())
                .enrollmentDate(req.getEnrollmentDate())
                .enrollmentStatus(req.getEnrollmentStatus() != null ? req.getEnrollmentStatus() : "active")
                .createdBy(userId)
                .build();
        return childRepository.save(child);
    }

    public Child update(Long id, ChildRequest req) {
        Child child = findById(id);
        child.setFirstName(req.getFirstName());
        child.setLastName(req.getLastName());
        child.setDateOfBirth(req.getDateOfBirth());
        child.setSex(req.getSex());
        child.setAddress(req.getAddress());
        child.setEnrollmentDate(req.getEnrollmentDate());
        if (req.getEnrollmentStatus() != null) child.setEnrollmentStatus(req.getEnrollmentStatus());
        return childRepository.save(child);
    }

    public void delete(Long id) {
        childRepository.deleteById(id);
    }
}
