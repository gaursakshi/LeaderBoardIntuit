package views;

import exceptions.DatabaseStorageException;
import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import models.PlayerScore;
import play.Logger;
import play.db.ebean.EbeanConfig;
import play.db.ebean.EbeanDynamicEvolutions;

import com.google.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class PlayerScoreRepository extends Model {

    private final Logger.ALogger logger = Logger.of(this.getClass());

    private final Finder<Long, PlayerScore> finder = new Finder<>(PlayerScore.class);

    private final EbeanConfig ebeanConfig;
    private final EbeanDynamicEvolutions ebeanDynamicEvolutions;


    @Inject
    public PlayerScoreRepository(EbeanConfig ebeanConfig, EbeanDynamicEvolutions ebeanDynamicEvolutions) {
        this.ebeanConfig = ebeanConfig;
        this.ebeanDynamicEvolutions = ebeanDynamicEvolutions;
    }


    public List<PlayerScore> findAll() {
        return finder.all();
    }

    public <E extends Model> Optional<E>findById(Class<E> modelClass, Map<String, Object> params) throws DatabaseStorageException {
        try {
            return Ebean.find(modelClass).where().allEq(params).findOneOrEmpty();
        }
        catch (Exception e){
            throw new DatabaseStorageException("Error in finding the player Record");
        }
    }
    public <E extends Model> void save(E entity , Optional<PlayerScore> scoreAlreadyPresent)  throws DatabaseStorageException  {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        if (scoreAlreadyPresent.isPresent()) {
            PlayerScore existingScore = scoreAlreadyPresent.get();
            if (entity.equals(existingScore)) {
                // Handle the case where the entity is unchanged, if needed
              throw new DatabaseStorageException("duplicate entry for the player!! Hence no insertion");
            }
            Ebean.update(entity);
        }
        else{
            Ebean.save(entity);
        }
    }

}
