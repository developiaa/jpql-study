package jpql;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Member member = new Member();
            member.setUsername("hello");
            member.setAge(10);
            em.persist(member);
            // 반환 타입이 명확할 때
            TypedQuery<Member> query = em.createQuery("select m from Member m", Member.class);
            List<Member> resultList = query.getResultList();
            for (Member member1 : resultList) {
                System.out.println("member1 = " + member1);
            }
            // 반환 타입이 명확하지 않을 때 사용
            Query query2 = em.createQuery("select m.age, m.username from Member m where m.id=1", Member.class);
            Object singleResult = query2.getSingleResult();
            System.out.println("singleResult = " + singleResult);


            // 파라미터 바인딩
            Member singleResult1 = em.createQuery("select m from Member m where m.username =:username", Member.class)
                    .setParameter("username", "hello").getSingleResult();
            System.out.println("singleResult1 = " + singleResult1.getUsername());


            // 프로젝션 - 여러 값 조회
            List<Object[]> resultList2 = em.createQuery("select m.username, m.age from Member m").getResultList();

            // 프로젝션 - 여러 값 조회 (DTO 사용)
            List<MemberDTO> result = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class).getResultList();
            MemberDTO memberDTO = result.get(0);


            // paging
            for (int i = 0; i < 100; i++) {
                Member member1 = new Member();
                member1.setUsername("member + " + i);
                member1.setAge(i);
                em.persist(member1);
            }
            em.flush();
            em.clear();
            em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(10)
                    .setMaxResults(20)
                    .getResultList();

            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member2 = new Member();
            member2.setUsername("member1");
            member2.setAge(10);
            member2.setTeam(team);
            member2.setType(MemberType.ADMIN);
            em.persist(member2);
            em.flush();
            em.clear();

//            String query3 = "select m from Member m inner join m.team t";
            // 조인대상 필터링
//            String query3 = "select m from Member m inner join m.team t on t.name='teamA'";
            // 연관관계 없는 엔티티 외부 조인
            String query3 = "select m from Member m left join Team t on m.username = t.name";
            List<Member> result3 = em.createQuery(query3, Member.class).getResultList();

            // JPQL 타입 표현 (하드 코딩 할 경우 패키지명 포함)
            String query4 = "select m.username, 'HELLO', true from Member m where m.type=jpql.MemberType.ADMIN";
            List<Member> resultList1 = em.createQuery(query4, Member.class).getResultList();


            // 조건식 - 기본 case 식
            String query5 = "select " +
                    "case when m.age <= 10 then '학생요금'" +
                    "     when m.age >= 60 then '경로요금'" +
                    "     else '일반요금'" +
                    "end " +
                    "from Member m";
            List<String> resultList3 = em.createQuery(query5, String.class).getResultList();

            // coalesce 하나씩 조회해서 null이 아니면 반환
            String query6 = "select coalesce(m.username, '이름 없는 회원') from Member m";
            List<String> resultList4 = em.createQuery(query6, String.class).getResultList();

            // size : 컬렉션의 갯수를 파악할 때 사용
            String query7 = "select size(t.members) from Team t";
            List<String> resultList5 = em.createQuery(query7, String.class).getResultList();

            // 사용자 정의 함수
            String query8 = "select function('group_concat',m.username) from Member m";
            // hibernate는 이렇게도 가능
            String query9 = "select group_concat(m.username) from Member m";
            List<Integer> resultList6 = em.createQuery(query8, Integer.class).getResultList();

            // 경로 표현식
            // m.username - 상태 필드, 단일값이므로 탐색 불가
            // m.team - 단일값 연관 경로 : 묵시적 내부 조인 발생, 탐색 가능
            // -> 단 실무에서는 쿼리 튜닝이 힘들기 때문에 명시적 조인을 쓰는게 좋다.

            String query10 = "select m.username from Member m";
            List<String> resultList7 = em.createQuery(query10, String.class).getResultList();

            // t.members - 컬렉션 값 연관 경로 : 묵시적 내부 조인 발생, 탐색 불가
            // -> from 절에서 명시적 조인을 통해 별칭을 통해 탐색 가능
            // select m.username From Team t join t.members m
            String query11 = "select t.members from Team t";
            Collection resultList8 = em.createQuery(query11, Collection.class).getResultList();


            // fetch join - 지연로딩 발생안함
            // 일반 조인은 연관된 엔티티를 함께 조회하지 않음
            // 페치 조인을 사용할때만 연관된 엔티티도 함께 조회(즉시 로딩)
            // -> 객체 그래프를 SQL 한번에 조회하는 개념
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀A");
            em.persist(teamB);

            Member member3 = new Member();
            member3.setUsername("회원1");
            member3.setTeam(teamA);

            Member member4 = new Member();
            member4.setUsername("회원2");
            member4.setTeam(teamA);

            Member member5 = new Member();
            member5.setUsername("회원3");
            member5.setTeam(teamB);

            String query12 = "select m from Member m join fetch m.team t";
            List<Member> resultList9 = em.createQuery(query12, Member.class).getResultList();

            for (Member member1 : resultList9) {
                // select m from Member m으로 조회하면
                // 회원1, 팀A(SQL)
                // 회원2, 팀A(1차캐시)
                // 회원3, 팀B(SQL)
                // 3번 조회함 -> N+1문제
                System.out.println("member1 = " + member1.getUsername() + " ," + member.getTeam().getName());
            }

            // 벌크 연산 수행 전 FLUSH 됨
            // 영속성 컨텍스트를 무시하고 DB에 직접 반영
            int resultCount = em.createQuery("update Member m set m.age=20").executeUpdate();
            
            em.clear(); // 벌크 연산 수행 후 영속성 컨텍스트 초기화 할 것
            Member findMember = em.find(Member.class, member2.getId());
            System.out.println("findMember.getAge() = " + findMember.getAge());
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }
}
