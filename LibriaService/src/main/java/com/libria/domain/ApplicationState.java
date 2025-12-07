package com.libria.domain;

import com.libria.exception.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import javax.security.auth.login.LoginException;
import java.util.List;

@ApplicationScoped
public class ApplicationState {

    @PersistenceContext(unitName = "LibriaPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        System.out.println("LibriaService - ApplicationState initialisé (aucune donnée seedée au démarrage)");
    }

    /* =====================================================
     *  ADMIN PAR DÉFAUT
     * ===================================================== */

    /**
     * Crée l'admin par défaut s'il n'existe pas déjà.
     * Admin : AD01 / "Super Admin" / admin@libria.com / libria123
     */
    @Transactional
    public void ensureDefaultAdminExists() {
        Long countAdmins = em.createQuery(
                "SELECT COUNT(a) FROM Admin a", Long.class
        ).getSingleResult();

        if (countAdmins == 0L) {
            Admin admin = new Admin(
                    "AD01",
                    "Super Admin",
                    "admin@libria.com",
                    "libria123"
            );
            em.persist(admin);
            System.out.println("LibriaService - Admin par défaut créé (admin@libria.com / libria123)");
        }
    }

    /* =====================================================
     *  BOOKS - LECTURE
     * ===================================================== */

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> listAllBooks() {
        return em.createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Book findBookByIsbn(String isbn) throws BookNotFoundException {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN invalide.");
        }
        Book b = em.find(Book.class, isbn);
        if (b == null) {
            throw new BookNotFoundException("Livre introuvable pour ISBN " + isbn);
        }
        return b;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> searchBooksByTitle(String title) {
        if (title == null) title = "";
        return em.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:t)",
                        Book.class)
                .setParameter("t", "%" + title + "%")
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> searchBooksByAuthor(String author) {
        if (author == null) author = "";
        return em.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(:a)",
                        Book.class)
                .setParameter("a", "%" + author + "%")
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> searchBooksByTitleOrAuthor(String q) {
        if (q == null) q = "";
        return em.createQuery("""
                SELECT b FROM Book b
                WHERE LOWER(b.title)  LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))
                """, Book.class)
                .setParameter("q", q)
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> searchBooksByGenre(String genre) {
        if (genre == null) genre = "";
        return em.createQuery("""
                SELECT b FROM Book b
                WHERE LOWER(b.genre) = LOWER(:g)
                """, Book.class)
                .setParameter("g", genre)
                .getResultList();
    }

    /* =====================================================
     *  USERS / MEMBERS
     * ===================================================== */

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<User> findAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public User findUserById(String userId) throws UserNotFoundException {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID utilisateur invalide.");
        }
        User u = em.find(User.class, userId);
        if (u == null) {
            throw new UserNotFoundException("Utilisateur introuvable : " + userId);
        }
        return u;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public User findUserByEmail(String email) throws UserNotFoundException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email invalide.");
        }

        User u = em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:mail)",
                        User.class)
                .setParameter("mail", email.toLowerCase())
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (u == null) {
            throw new UserNotFoundException("Aucun utilisateur trouvé avec cet email.");
        }
        return u;
    }

    @Transactional
    public void registerMember(Member m) throws UserAlreadyExistException {
        if (m == null) {
            throw new IllegalArgumentException("Member non fourni.");
        }

        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = LOWER(:email)",
                        Long.class
                ).setParameter("email", m.getEmail())
                .getSingleResult();

        if (count > 0) {
            throw new UserAlreadyExistException("Email déjà utilisé : " + m.getEmail());
        }

        em.persist(m);
    }

    @Transactional
    public void changePassword(String userId, String newPassword) throws UserNotFoundException {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Nouveau mot de passe invalide.");
        }
        User u = findUserById(userId);
        u.setPassword(newPassword);
        // pas besoin de merge, l'entité est gérée
    }

    @Transactional
    public void deleteUser(String userId) throws UserNotFoundException {
        User u = findUserById(userId);
        em.remove(u);
    }

    /* =====================================================
     *  DOWNLOADS (livres téléchargés par user)
     * ===================================================== */

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> listDownloads(String userId) throws UserNotFoundException {
        User u = findUserById(userId);
        return u.listDownloadedBooks(); // ManyToMany gérée par JPA
    }

    @Transactional
    public void addDownload(String userId, String isbn)
            throws UserNotFoundException, BookNotFoundException, BookAlreadyExistException {

        User u = findUserById(userId);

        Book b = em.find(Book.class, isbn);
        if (b == null) {
            throw new BookNotFoundException("Livre introuvable : " + isbn);
        }

        u.downloadBook(b); // peut lancer BookAlreadyExistException
    }

    @Transactional
    public void removeDownload(String userId, String isbn)
            throws UserNotFoundException, BookNotFoundException {

        User u = findUserById(userId);

        Book b = em.find(Book.class, isbn);
        if (b == null) {
            throw new BookNotFoundException("Livre introuvable : " + isbn);
        }

        u.removeBook(b);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> searchDownloads(String userId, String title, String genre)
            throws UserNotFoundException {

        User u = findUserById(userId);
        var list = u.listDownloadedBooks();

        if ((title == null || title.isBlank()) && (genre == null || genre.isBlank())) {
            return list;
        }

        String t = (title == null) ? "" : title.toLowerCase();
        String g = (genre == null) ? "" : genre.toLowerCase();

        return list.stream()
                .filter(b ->
                        (t.isBlank() || b.getTitle().toLowerCase().contains(t)) &&
                                (g.isBlank() || g.equalsIgnoreCase(b.getGenre()))
                )
                .toList();
    }

    /* =====================================================
     *  CRUD LIVRES (admin uniquement)
     * ===================================================== */

    @Transactional
    public void addBook(String userId, Book book)
            throws AccessDeniedException, BookAlreadyExistException {

        if (book == null) {
            throw new IllegalArgumentException("Book non fourni.");
        }

        User user = em.find(User.class, userId);
        if (user == null || !(user instanceof Admin)) {
            throw new AccessDeniedException("Seul un administrateur peut ajouter des livres.");
        }

        if (em.find(Book.class, book.getIsbn()) != null) {
            throw new BookAlreadyExistException("Livre déjà existant !");
        }

        em.persist(book);
    }

    @Transactional
    public void removeBook(String userId, String isbn)
            throws AccessDeniedException, BookNotFoundException {

        User admin = em.find(User.class, userId);
        if (admin == null || !(admin instanceof Admin)) {
            throw new AccessDeniedException("Seul un administrateur peut supprimer des livres.");
        }

        Book existing = em.find(Book.class, isbn);
        if (existing == null) {
            throw new BookNotFoundException("Livre introuvable !");
        }

        // 1) Enlever le livre de tous les téléchargements
        var users = em.createQuery("""
                SELECT u FROM User u
                JOIN u.downloadedBooks b
                WHERE b.isbn = :isbn
            """, User.class)
                .setParameter("isbn", isbn)
                .getResultList();

        for (User u : users) {
            u.removeBook(existing);  // met à jour la table de jointure
        }

        // 2) Supprimer le livre
        em.remove(existing);
    }
    @Transactional
    public User updateProfile(String userId, String newName, String newEmail)
            throws UserNotFoundException, UserAlreadyExistException {

        User u = findUserById(userId);  // entity MANAGÉE

        if (newName != null && !newName.isBlank()) {
            u.setName(newName);
        }

        if (newEmail != null && !newEmail.isBlank()) {

            // Vérifier que l'email n'est pas déjà utilisé par un autre user
            Long count = em.createQuery(
                            "SELECT COUNT(u2) FROM User u2 " +
                                    "WHERE LOWER(u2.email) = LOWER(:mail) " +
                                    "AND u2.userId <> :id",
                            Long.class)
                    .setParameter("mail", newEmail.toLowerCase())
                    .setParameter("id", userId)
                    .getSingleResult();

            if (count > 0) {
                throw new UserAlreadyExistException("Email déjà utilisé : " + newEmail);
            }

            u.setEmail(newEmail);
        }

        // Pas besoin de em.merge(u) : u est MANAGÉ + @Transactional
        return u;
    }
    @Transactional
    public void updateBook(String userId, String isbn, Book updated)
            throws AccessDeniedException, BookNotFoundException {

        if (updated == null) {
            throw new IllegalArgumentException("Book mis à jour non fourni.");
        }

        User user = em.find(User.class, userId);
        if (user == null || !(user instanceof Admin)) {
            throw new AccessDeniedException("Seul un administrateur peut modifier des livres.");
        }

        Book existing = em.find(Book.class, isbn);
        if (existing == null) {
            throw new BookNotFoundException("Livre introuvable !");
        }

        if (updated.getTitle() != null && !updated.getTitle().isBlank()) {
            existing.setTitle(updated.getTitle());
        }
        if (updated.getAuthor() != null && !updated.getAuthor().isBlank()) {
            existing.setAuthor(updated.getAuthor());
        }
        if (updated.getGenre() != null && !updated.getGenre().isBlank()) {
            existing.setGenre(updated.getGenre());
        }
        if (updated.getYear() > 0) {
            existing.setYear(updated.getYear());
        }
        if (updated.getPdf() != null && !updated.getPdf().isBlank()) {
            existing.setPdf(updated.getPdf());
        }
        if (updated.getCoverImage() != null && !updated.getCoverImage().isBlank()) {
            existing.setCoverImage(updated.getCoverImage());
        }
        if (updated.isAvailable() != existing.isAvailable()) {
            existing.setAvailable(updated.isAvailable());
        }

        em.merge(existing);
    }

    /* =====================================================
     *  AUTHENTIFICATION
     * ===================================================== */

    @Transactional
    public User authenticate(String email, String password)
            throws UserNotFoundException, LoginException {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email et mot de passe requis.");
        }

        // S'assure qu'il y a au moins l'admin par défaut en BDD
        ensureDefaultAdminExists();

        User u = em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.email) = :mail",
                        User.class)
                .setParameter("mail", email.toLowerCase())
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (u == null) {
            throw new UserNotFoundException("Utilisateur introuvable pour cet email.");
        }

        u.login(password);  // peut lancer LoginException
        return u;
    }
}