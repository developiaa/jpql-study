package jpql;

import javax.persistence.*;
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
            String query5 = "select "+
                                    "case when m.age <= 10 then '학생요금'"+
                                    "     when m.age >= 60 then '경로요금'"+
                                    "     else '일반요금'" +
                                    "end "+
                                "from Member m";
            List<String> resultList3 = em.createQuery(query5, String.class).getResultList();

            // coalesce 하나씩 조회해서 null이 아니면 반환
            String query6 = "select coalesce(m.username, '이름 없는 회원') from Member m";
            List<String> resultList4 = em.createQuery(query6, String.class).getResultList();

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
