package rs.vs.meetings_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.vs.meetings_service.dto.ParticipationCountDto;
import rs.vs.meetings_service.repository.MeetingRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final MeetingRepository meetingRepository;
    public ParticipationCountDto weekly(Long userId) {
        LocalDate now = LocalDate.now();
        return new ParticipationCountDto("nedeljno", meetingRepository.countParticipationInRange(userId, now.minusDays(7), now));
    }

    public ParticipationCountDto monthly(Long userId) {
        LocalDate now = LocalDate.now();
        return new ParticipationCountDto("mesecno", meetingRepository.countParticipationInRange(userId, now.withDayOfMonth(1), now));
    }

    public ParticipationCountDto yearly(Long userId) {
        LocalDate now = LocalDate.now();
        return new ParticipationCountDto("godisnje", meetingRepository.countParticipationInRange(userId, now.withDayOfYear(1), now));
    }
}
