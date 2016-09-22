package pl.wesolucky.shop;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.wesolucky.shop.domain.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long>
{
}
