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
